package com.palm1.analogaudio.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.item.WalkieTalkieItem;
import com.palm1.analogaudio.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Matrix4f;

public class WalkieTalkieRenderer extends BlockEntityWithoutLevelRenderer {

    public static final double SCREEN_X_CENTER = 0.5D;
    public static final double SCREEN_Y_OFFSET = 3.2D / 16.0D;
    public static final double SCREEN_Z_OFFSET = 3.15D / 16.0D;

    public static final float TEXT_SCALE = 0.025f;
    public static final float INDICATOR_SCALE = 0.015f;

    public static final float FREQ_X_OFFSET = 0f;
    public static final float FREQ_Y_OFFSET = -3.5f;

    public static final float INDICATOR_X_POS = 7f;
    public static final float INDICATOR_Y_POS = -5f;

    public static final float BUTTON_TRAVEL_MAX = 0.9f / 16.0f;

    public static final ResourceLocation BODY_MODEL = new ResourceLocation(AnalogAudio.MODID,
            "item/walkie_talkie_body");
    public static final ResourceLocation BUTTON_MODEL = new ResourceLocation(AnalogAudio.MODID,
            "item/walkie_talkie_button");

    private static float currentButtonOffset = 0f;
    private static float buttonVelocity = 0f;
    private static long lastFrameTime = -1;
    private boolean wasPttActive = false;

    public WalkieTalkieRenderer(ItemRenderer itemRenderer, EntityModelSet entityModelSet) {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), entityModelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        BakedModel bodyModel = itemRenderer.getItemModelShaper().getModelManager().getModel(BODY_MODEL);
        BakedModel buttonModel = itemRenderer.getItemModelShaper().getModelManager().getModel(BUTTON_MODEL);

        boolean pttActive = getPttFallback();
        boolean isTalking = getTalkingFallback();
        boolean isMuted = getMuteFallback();

        boolean isHeldInHand = mc.player != null
                && (mc.player.getMainHandItem() == stack || mc.player.getOffhandItem() == stack);

        boolean isFirstPerson = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean isThirdPerson = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        boolean isGui = displayContext == ItemDisplayContext.GUI;
        boolean isHandContext = isFirstPerson || isThirdPerson;

        if (!isHeldInHand) {
            pttActive = false;
            isTalking = false;
        }

        if (isHeldInHand && mc.player != null) {
            if (isHandContext) {
                if (pttActive && !wasPttActive) {
                    mc.player.playSound(ModSounds.WALKIE_PRESS.get(), 0.4f, (float) (Math.random() * 0.125f + 1.0f));
                } else if (!pttActive && wasPttActive) {
                    mc.player.playSound(ModSounds.WALKIE_UNPRESS.get(), 0.4f, (float) (Math.random() * 0.0625f + 1.0f));
                }
            }
            wasPttActive = pttActive;
        } else {
            wasPttActive = false;
        }

        long now = mc.level != null ? mc.level.getGameTime() : -1;
        if (now != -1 && now != lastFrameTime) {
            float dt = 0.05f;
            lastFrameTime = now;
            float target = pttActive ? BUTTON_TRAVEL_MAX : 0.0f;
            float stiffness = 180.0f;
            float damping = 15.0f;
            float force = (target - currentButtonOffset) * stiffness;
            buttonVelocity += (force - buttonVelocity * damping) * dt;
            currentButtonOffset += buttonVelocity * dt;
        }

        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.cutout());
        itemRenderer.renderModelLists(bodyModel, stack, packedLight, packedOverlay, poseStack, vertexConsumer);

        poseStack.pushPose();
        float visualOffset = isHeldInHand ? currentButtonOffset : 0;
        poseStack.translate(0, 0, visualOffset);
        itemRenderer.renderModelLists(buttonModel, stack, packedLight, packedOverlay, poseStack, vertexConsumer);
        poseStack.popPose();

        if (isFirstPerson || isThirdPerson || isGui) {
            renderScreen(stack, poseStack, bufferSource, isTalking, pttActive, isMuted, packedLight, mc);
        }

        poseStack.popPose();
    }

    private void renderScreen(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, boolean isTalking,
            boolean pttActive, boolean isMuted, int light, Minecraft mc) {
        poseStack.pushPose();

        int frequency = WalkieTalkieItem.getFrequency(stack);
        String freqStr = String.valueOf(frequency);

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));
        poseStack.translate(0.0D, SCREEN_Y_OFFSET, SCREEN_Z_OFFSET);

        boolean isTransmitting = isTalking || (pttActive && !isMuted);
        boolean isBlinking = !isTransmitting && !isMuted && (mc.level != null && mc.level.getGameTime() % 20 < 10);
        boolean showIndicator = isTransmitting || isBlinking;

        Font font = mc.font;
        int fullBright = 15728880;

        float textWidth = (float) font.width(freqStr);
        float xPos = -textWidth / 2f + FREQ_X_OFFSET;

        poseStack.pushPose();
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, 1.0f);

        int nixieBright = 0xFFFF6A00;
        int nixieCore = 0xFFFFA133;
        int nixieDark = 0xFF9E4300;

        poseStack.pushPose();
        poseStack.translate(0, 0, 0.001f);
        font.drawInBatch(freqStr, xPos + 0.1f, FREQ_Y_OFFSET + 0.1f, nixieDark, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, fullBright);
        poseStack.translate(0, 0, 0.001f);
        font.drawInBatch(freqStr, xPos, FREQ_Y_OFFSET, nixieBright, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, fullBright);
        poseStack.translate(0, 0, 0.001f);
        font.drawInBatch(freqStr, xPos, FREQ_Y_OFFSET, nixieCore, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, fullBright);
        poseStack.popPose();

        poseStack.popPose();

        poseStack.pushPose();
        poseStack.scale(INDICATOR_SCALE, -INDICATOR_SCALE, 1.0f);
        Matrix4f matrix4f = poseStack.last().pose();
        int dotColor = showIndicator ? 0xFFFF0000 : 0xFF440000;

        float scaleAdjustment = TEXT_SCALE / INDICATOR_SCALE;
        font.drawInBatch("\u25cf", INDICATOR_X_POS * scaleAdjustment, INDICATOR_Y_POS * scaleAdjustment, dotColor,
                false, matrix4f, buffer, Font.DisplayMode.NORMAL, 0, fullBright);
        poseStack.popPose();

        poseStack.popPose();
    }

    private static Boolean getPttFallback() {
        try {
            Class<?> clientManagerClass = Class.forName("de.maxhenkel.voicechat.voice.client.ClientManager");
            Object instance = clientManagerClass.getMethod("instance").invoke(null);
            Object pttHandler = clientManagerClass.getMethod("getPttKeyHandler").invoke(instance);
            return (boolean) pttHandler.getClass().getMethod("isPTTDown").invoke(pttHandler);
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean getMuteFallback() {
        try {
            Class<?> clientManagerClass = Class.forName("de.maxhenkel.voicechat.voice.client.ClientManager");
            Object instance = clientManagerClass.getMethod("instance").invoke(null);
            Object playerState = clientManagerClass.getMethod("getPlayerStateManager").invoke(instance);
            return (boolean) playerState.getClass().getMethod("isMuted").invoke(playerState);
        } catch (Exception e) {
            return false;
        }
    }

    private static Boolean getTalkingFallback() {
        try {
            Class<?> clientManagerClass = Class.forName("de.maxhenkel.voicechat.voice.client.ClientManager");
            Object client = clientManagerClass.getMethod("getClient").invoke(null);
            if (client == null)
                return false;
            Object micThread = client.getClass().getMethod("getMicThread").invoke(client);
            if (micThread == null)
                return false;
            return (boolean) micThread.getClass().getMethod("shouldTransmitAudio").invoke(micThread);
        } catch (Exception e) {
            return false;
        }
    }
}
