package com.palm1.analogaudio.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.math.Axis;
import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.registry.ModSounds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class RotaryTunerScreen extends Screen {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AnalogAudio.MODID,
            "textures/gui/rotary_frequency.png");

    private int ticksOpen = 0;
    private int soundCoolDown = 0;
    private final Consumer<Integer> frequencySaver;
    private int frequency;

    private static final float POINTER_ANGLE = 214.0f;
    private static final float START_ANGLE = 214.0f;
    private static final float STEP_ANGLE = 28.5f;
    private static final float HOLE_RADIUS = 66.5f;

    private static final int HOLE_U = 210;
    private static final int HOLE_V = 4;
    private static final int HOLE_SIZE = 38;

    private float currentRotation = 0;
    private float targetRotation = 0;

    public RotaryTunerScreen(int initialFrequency, Consumer<Integer> frequencySaver) {
        super(Component.translatable("gui.analogaudio.rotary_tuner.title"));
        this.frequency = Mth.clamp(initialFrequency, 1, 9);
        this.frequencySaver = frequencySaver;
        this.targetRotation = getAngleForFrequency(this.frequency);
        this.currentRotation = this.targetRotation;
    }

    @Override
    protected void init() {
        super.init();
        setCursorToFreq(this.frequency);
    }

    private void setCursorToFreq(int freq) {
        int centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        int centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2;

        float nativeHoleAngle = START_ANGLE + (freq - 1) * STEP_ANGLE;
        double rad = Math.toRadians(nativeHoleAngle - 90);
        float hX = (float) (Math.cos(rad) * HOLE_RADIUS);
        float hY = (float) (Math.sin(rad) * HOLE_RADIUS);
        setCursor(new Vec2(centerX + hX, centerY + hY));
    }

    private float getAngleForFrequency(int freq) {
        return POINTER_ANGLE - (START_ANGLE + (freq - 1) * STEP_ANGLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int darkAlpha = (int) (0x50 * Math.min(1, (ticksOpen + partialTicks) / 20f));
        int backgroundColor = (darkAlpha << 24) | 0x101010;
        graphics.fillGradient(0, 0, this.width, this.height, backgroundColor, backgroundColor);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float renderRotation = Mth.lerp(0.4f, currentRotation, targetRotation);
        currentRotation = renderRotation;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 100);
        graphics.pose().pushPose();
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(renderRotation));
        graphics.blit(TEXTURE, -90, -90, 0, 0, 180, 180, 256, 256);

        for (int i = 1; i <= 9; i++) {
            float holeAngleOnWheel = START_ANGLE + (i - 1) * STEP_ANGLE;
            double rad = Math.toRadians(holeAngleOnWheel - 90);
            float hX = (float) (Math.cos(rad) * HOLE_RADIUS);
            float hY = (float) (Math.sin(rad) * HOLE_RADIUS);

            graphics.blit(TEXTURE, (int) hX - HOLE_SIZE / 2, (int) hY - HOLE_SIZE / 2, HOLE_U, HOLE_V, HOLE_SIZE,
                    HOLE_SIZE, 256, 256);
        }
        graphics.pose().popPose();

        for (int i = 1; i <= 9; i++) {
            float worldHoleAngle = START_ANGLE + (i - 1) * STEP_ANGLE + renderRotation;
            double rad = Math.toRadians(worldHoleAngle - 90);
            float hX = (float) (Math.cos(rad) * HOLE_RADIUS);
            float hY = (float) (Math.sin(rad) * HOLE_RADIUS);

            String text = String.valueOf(i);
            int tw = font.width(text);
            graphics.drawString(font, text, (int) hX - tw / 2, (int) hY - 4, 0x442000, false);
        }

        graphics.pose().pushPose();
        graphics.pose().mulPose(Axis.ZP.rotationDegrees(POINTER_ANGLE));
        graphics.blit(TEXTURE, -8, -80, 181, 0, 16, 80, 256, 256);
        graphics.pose().popPose();
        graphics.blit(TEXTURE, -12, -12, 181, 81, 24, 24, 256, 256);
        graphics.pose().popPose();

        if (ticksOpen > 1) {
            updateFrequencyFromMouse(mouseX, mouseY);
        }
    }

    private void setCursor(Vec2 pos) {
        Window window = minecraft.getWindow();
        double guiScale = window.getGuiScale();
        GLFW.glfwSetCursorPos(window.getWindow(), pos.x * guiScale, pos.y * guiScale);
    }

    private void updateFrequencyFromMouse(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 30 && dist < 120) {
            double angle = Math.toDegrees(Math.atan2(dy, dx)) + 90;
            if (angle < 0)
                angle += 360;

            int bestFreq = this.frequency;
            float bestD = Float.MAX_VALUE;

            for (int i = 1; i <= 9; i++) {
                float nativeHoleAngle = START_ANGLE + (i - 1) * STEP_ANGLE;
                float diff = Math.abs(Mth.degreesDifference(nativeHoleAngle, (float) angle));
                if (diff < bestD) {
                    bestD = diff;
                    bestFreq = i;
                }
            }

            if (bestFreq != this.frequency) {
                this.frequency = bestFreq;
                this.targetRotation = getAngleForFrequency(this.frequency);
                setCursorToFreq(this.frequency);

                if (soundCoolDown == 0) {
                    float pitch = 0.5f + (this.frequency / 9f);
                    minecraft.getSoundManager()
                            .play(SimpleSoundInstance.forUI(ModSounds.FREQUENCY_TICK.get(), pitch, 1.0F));
                    soundCoolDown = 2;
                }
            }
        }
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (soundCoolDown > 0)
            soundCoolDown--;

        long window = Minecraft.getInstance().getWindow().getWindow();
        if (GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) != GLFW.GLFW_PRESS) {
            this.onClose();
        }
    }

    @Override
    public void onClose() {
        this.frequencySaver.accept(this.frequency);
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.FREQUENCY_SELECT.get(), 1.0F, 1.0F));
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics graphics) {
    }
}
