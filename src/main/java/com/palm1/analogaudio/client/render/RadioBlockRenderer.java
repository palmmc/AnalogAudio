package com.palm1.analogaudio.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import com.palm1.analogaudio.block.RadioBlock;
import com.palm1.analogaudio.block.entity.RadioBlockEntity;
import com.palm1.analogaudio.config.ModConfig;
import com.palm1.analogaudio.item.CassetteData;

public class RadioBlockRenderer implements BlockEntityRenderer<RadioBlockEntity> {
    private final Font font;

    public RadioBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(RadioBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        ItemStack cassette = entity.getCassette();
        if (cassette.isEmpty()) {
            long timeSinceRemove = entity.getLevel().getGameTime() - entity.getRemoveTime();
            if (timeSinceRemove < 10) {
                int light = ModRenderUtils.getFacingLight(entity, entity.getBlockState());
                renderCassette(entity, cassette, poseStack, buffer, light, combinedOverlay,
                        1.0f - (timeSinceRemove + partialTick) / 10f, 1.0f);
            }
            return;
        }

        long timeSinceInsert = entity.getLevel().getGameTime() - entity.getInsertTime();
        float animProgress = Mth.clamp((timeSinceInsert + partialTick) / 10f, 0f, 1f);

        int light = ModRenderUtils.getFacingLight(entity, entity.getBlockState());
        renderCassette(entity, cassette, poseStack, buffer, light, combinedOverlay, 1.0f, animProgress);
    }

    private void renderCassette(RadioBlockEntity entity, ItemStack cassette, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight, int combinedOverlay, float alpha, float animProgress) {
        poseStack.pushPose();
        Direction facing = entity.getBlockState().getValue(RadioBlock.FACING);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        float springProgress = 1.0f;
        if (ModConfig.CLIENT_CONFIG.enableCassetteAnimation.get()) {
            springProgress = ModRenderUtils.calculateBackOut(animProgress);
        }

        float zOffset = Mth.lerp(springProgress, 0.76f, 0.51f);
        poseStack.translate(0, 0.25, zOffset);

        poseStack.mulPose(Axis.XP.rotationDegrees(90));

        poseStack.scale(0.86f, 0.86f, 0.86f);

        BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(new ResourceLocation("analogaudio", "item/cassette_tape_3d"));

        Minecraft.getInstance().getItemRenderer().render(
                cassette,
                ItemDisplayContext.NONE,
                false,
                poseStack,
                buffer,
                combinedLight,
                combinedOverlay,
                model);

        CassetteData data = CassetteData.get(cassette);

        if (data != null) {
            String label = data.name();
            if (label.length() > 20)
                label = label.substring(0, 17) + "...";

            poseStack.pushPose();
            poseStack.translate(0.0, 0.01, -0.03);
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.scale(-0.012f, 0.012f, 0.012f);

            float width = font.width(label);
            font.drawInBatch(label, -width / 2, 0, 0xFFFFFFFF, false, poseStack.last().pose(), buffer,
                    Font.DisplayMode.NORMAL, 0, combinedLight);
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
