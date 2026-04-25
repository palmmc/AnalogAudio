package com.palm1.analogaudio.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.palm1.analogaudio.block.entity.SpeakerBlockEntity;
import com.palm1.analogaudio.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SpeakerBlockRenderer implements BlockEntityRenderer<SpeakerBlockEntity> {
        @SuppressWarnings("unused")
        private final BlockRenderDispatcher blockRenderer;

        public SpeakerBlockRenderer(BlockEntityRendererProvider.Context context) {
                this.blockRenderer = context.getBlockRenderDispatcher();
        }

        @Override
        public void render(SpeakerBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                        MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
                Level mainLevel = Minecraft.getInstance().level;
                BlockState state = blockEntity.getBlockState();
                long globalTime = mainLevel != null ? mainLevel.getGameTime() : 0;

                float currentScale = 1.0f;
                if (ModConfig.CLIENT_CONFIG.enableSpeakerAnimation.get()) {
                        currentScale = blockEntity.updateAndGetAnimationScale(globalTime, partialTick, 1.0f);
                }
                ModRenderUtils.renderBlockModel(blockEntity, state, currentScale, poseStack, bufferSource,
                                packedOverlay);

                poseStack.pushPose();
                Direction facing = blockEntity.getActiveSide();
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
                poseStack.translate(0.0f, 0.0f, 0.51f);
                float textScale = 0.05f;
                poseStack.scale(textScale, -textScale, textScale);

                float opacity = Mth.lerp(partialTick, blockEntity.getPrevDisplayOpacity(),
                                blockEntity.getDisplayOpacity());
                if (opacity > 0.01f) {
                        String frequencyText = String.valueOf(blockEntity.getFrequency());
                        renderNixieText(frequencyText, opacity, poseStack, bufferSource);
                }

                poseStack.popPose();
        }

        private void renderNixieText(String text, float opacity, PoseStack poseStack, MultiBufferSource bufferSource) {
                int alpha = (int) (255 * opacity) << 24;
                Font font = Minecraft.getInstance().font;
                float xOffset = (float) (-font.width(text) / 2);

                int nixieBright = (alpha) | 0xFF6A00;
                int nixieCore = (alpha) | 0xFFA133;
                int nixieDark = (alpha) | 0x9E4300;
                int fullBright = 15728880;

                float yOffset = -4.5f;
                poseStack.pushPose();
                poseStack.translate(0, 0, 0.01f);
                font.drawInBatch(text, xOffset, yOffset + 0.5f, nixieDark, false, poseStack.last().pose(),
                                bufferSource, Font.DisplayMode.NORMAL, 0, fullBright);

                poseStack.translate(0, 0, 0.01f);
                font.drawInBatch(text, xOffset, yOffset, nixieBright, false, poseStack.last().pose(),
                                bufferSource, Font.DisplayMode.NORMAL, 0, fullBright);

                poseStack.translate(0, 0, 0.01f);
                font.drawInBatch(text, xOffset, yOffset, nixieCore, false, poseStack.last().pose(),
                                bufferSource, Font.DisplayMode.NORMAL, 0, fullBright);
                poseStack.popPose();
        }
}
