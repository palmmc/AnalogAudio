package com.palm1.analogaudio.integration.voicechat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.palm1.analogaudio.client.render.WalkieTalkieRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ModelEvent;

public class VoicechatClientHooks {
    private static WalkieTalkieRenderer walkieTalkieRenderer;

    public static void renderWalkieTalkie(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (walkieTalkieRenderer == null) {
            walkieTalkieRenderer = new WalkieTalkieRenderer(
                    Minecraft.getInstance().getItemRenderer(),
                    Minecraft.getInstance().getEntityModels());
        }
        walkieTalkieRenderer.renderByItem(stack, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(WalkieTalkieRenderer.BODY_MODEL);
        event.register(WalkieTalkieRenderer.BUTTON_MODEL);
    }
}
