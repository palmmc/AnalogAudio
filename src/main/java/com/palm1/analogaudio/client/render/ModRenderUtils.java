package com.palm1.analogaudio.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class ModRenderUtils {
    public static int getFacingLight(BlockEntity entity, BlockState state) {
        Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
        return LevelRenderer.getLightColor(entity.getLevel(), entity.getBlockPos().relative(facing));
    }

    public static float calculateBackOut(float progress) {
        float t = progress - 1.0f;
        float s = 1.70158f;
        return t * t * ((s + 1.0f) * t + s) + 1.0f;
    }

    public static void renderBlockModel(BlockEntity entity, BlockState state, float scale, PoseStack poseStack,
            MultiBufferSource buffer, int packedOverlay) {
        Level level = entity.getLevel();
        BlockPos pos = entity.getBlockPos();

        poseStack.pushPose();
        if (scale != 1.0f) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5, -0.5, -0.5);
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(state);
        RenderType renderType = RenderType.cutout();

        dispatcher.getModelRenderer().tesselateBlock(
                level,
                model,
                state,
                pos,
                poseStack,
                buffer.getBuffer(renderType),
                false,
                level.getRandom(),
                state.getSeed(pos),
                packedOverlay,
                ModelData.EMPTY,
                renderType);

        poseStack.popPose();
    }
}
