package com.palm1.analogaudio.block.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;

import com.palm1.analogaudio.block.SpeakerBlock;
import com.palm1.analogaudio.client.audio.ClientAudioEngine;
import com.palm1.analogaudio.integration.voicechat.SpeakerInstance;
import com.palm1.analogaudio.integration.voicechat.SpeakerManager;
import com.palm1.analogaudio.registry.ModBlockEntities;

public class SpeakerBlockEntity extends BlockEntity implements SpeakerInstance {
    private int frequency = 1;
    private float scale = 1.0f;
    private long lastVoiceTime = 0;

    public SpeakerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SPEAKER.get(), pos,
                blockState);
    }

    public int getFrequency() {
        return frequency;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return this.scale;
    }

    public void setFrequency(int frequency) {
        int oldFreq = this.frequency;
        this.frequency = Mth.clamp(frequency, 1, 255);
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            SpeakerManager.updateSpeakerFrequency(this, oldFreq,
                    this.frequency);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level != null) {
            SpeakerManager.addSpeaker(this);
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null) {
            SpeakerManager.removeSpeaker(this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Frequency", this.frequency);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Frequency")) {
            this.frequency = tag.getInt("Frequency");
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Vec3 getPosition() {
        return Vec3.atCenterOf(this.worldPosition);
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    private float displayOpacity = 0f;
    private float prevDisplayOpacity = 0f;
    private float animationScale = 1.0f;
    private float prevAnimationScale = 1.0f;
    private Direction activeSide = Direction.NORTH;

    public float getDisplayOpacity() {
        return displayOpacity;
    }

    public float getPrevDisplayOpacity() {
        return prevDisplayOpacity;
    }

    public Direction getActiveSide() {
        return activeSide;
    }

    public float updateAndGetAnimationScale(long time, float partialTick, float target) {
        return Mth.lerp(partialTick, prevAnimationScale, animationScale);
    }

    public void clientTick() {
        this.prevDisplayOpacity = this.displayOpacity;
        this.prevAnimationScale = this.animationScale;

        float engineLoudness = ClientAudioEngine.getLoudness(getIdentity());
        float targetPulse = Math.max(this.scale, 1.0f + (engineLoudness * 0.15f));

        targetPulse = Mth.clamp(targetPulse, 1.0f, 1.15f);

        if (targetPulse > this.animationScale) {
            this.animationScale = targetPulse;
        } else {
            this.animationScale = Mth.lerp(0.2f, this.animationScale, 1.0f);
        }

        this.scale = Mth.lerp(0.2f, this.scale, 1.0f);

        boolean lookingAt = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK
                && mc.hitResult instanceof BlockHitResult bhr) {
            BlockPos hitPos = bhr.getBlockPos();
            if (hitPos.getX() == this.worldPosition.getX() && hitPos.getY() == this.worldPosition.getY()
                    && hitPos.getZ() == this.worldPosition.getZ()) {
                lookingAt = true;

                Direction hitSide = bhr.getDirection();
                if (hitSide.getAxis().isHorizontal()) {
                    this.activeSide = hitSide;
                }
            }
        }

        float fadeSpeed = 0.1f;
        if (lookingAt) {
            this.displayOpacity = Math.min(1.0f, this.displayOpacity + fadeSpeed);
        } else {
            this.displayOpacity = Math.max(0.0f, this.displayOpacity - fadeSpeed);
        }
    }

    @Override
    public Object getIdentity() {
        if (this.level == null) return "speaker_unknown";
        return "speaker_" + this.level.dimension().location() + "_"
                + this.worldPosition.toShortString().replace(" ", "");
    }

    @Override
    public void onVoicePacketReceived() {
        if (this.level != null && !this.level.isClientSide) {
            this.lastVoiceTime = this.level.getGameTime();
            BlockState state = getBlockState();
            if (state.hasProperty(SpeakerBlock.POWERED) && !state.getValue(SpeakerBlock.POWERED)) {
                this.level.setBlock(this.worldPosition, state.setValue(SpeakerBlock.POWERED, true), 3);
                this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
                this.level.scheduleTick(this.worldPosition, state.getBlock(), 10);
            }
        }
    }

    public long getLastVoiceTime() {
        return lastVoiceTime;
    }
}
