package com.palm1.analogaudio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.Containers;

import org.jetbrains.annotations.Nullable;

import com.palm1.analogaudio.inventory.RadioMenu;
import com.palm1.analogaudio.block.RadioBlock;
import com.palm1.analogaudio.client.audio.ClientAudioEngine;
import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.registry.ModBlockEntities;
import com.palm1.analogaudio.registry.ModSounds;
import com.palm1.analogaudio.registry.ModItems;

public class RadioBlockEntity extends BlockEntity implements MenuProvider {
    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(ModItems.CASSETTE_TAPE.get());
        }
    };
    private long startTime = 0;
    private float volume = 0.75f;
    private boolean looping = false;
    private boolean wasPowered = false;
    private boolean playing = false;
    private long pausedOffset = 0;
    private boolean wasEmpty = true;
    private ItemStack lastCassette = ItemStack.EMPTY;
    private long insertTime = 0;
    private long removeTime = 0;

    public RadioBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.RADIO.get(), pos, blockState);

        this.wasEmpty = inventory.isEmpty();
        this.lastCassette = getCassette().copy();

        inventory.addListener(container -> {
            ItemStack currentCassette = getCassette();
            boolean isEmpty = currentCassette.isEmpty();
            boolean itemChanged = !ItemStack.matches(currentCassette, lastCassette);
            CassetteData data = CassetteData.get(currentCassette);
            boolean hasData = data != null;

            if (this.level != null && !this.level.isClientSide()) {
                if (itemChanged) {
                    if (this.wasEmpty && !isEmpty) {
                        this.insertTime = this.level.getGameTime();
                    } else if (!this.wasEmpty && isEmpty) {
                        this.removeTime = this.level.getGameTime();
                    }
                }

                if (hasData) {
                    this.level.playSound(null, this.worldPosition, ModSounds.CASSETTE_INSERT.get(),
                            SoundSource.BLOCKS, 0.5f, 1.0f);
                    this.playing = true;
                    this.startTime = this.level.getGameTime();
                    this.pausedOffset = 0;
                } else {
                    if (isEmpty) {
                        this.level.playSound(null, this.worldPosition, ModSounds.CASSETTE_EJECT.get(),
                                SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                    this.playing = false;
                    this.startTime = 0;
                    this.pausedOffset = 0;
                }
                setChanged();
                updatePowerState();
                updateAndSync();
            }

            this.lastCassette = currentCassette.copy();
            this.wasEmpty = isEmpty;
        });
    }

    public void setSettings(float volume, boolean looping, boolean playing) {
        if (this.level != null && this.playing != playing) {
            if (playing) {
                this.startTime = this.level.getGameTime() - this.pausedOffset;
            } else {
                this.pausedOffset = this.level.getGameTime() - this.startTime;
            }
        }
        this.playing = playing;
        this.volume = volume;
        this.looping = looping;
        updatePowerState();
        updateAndSync();
    }

    public void setPowered(boolean powered) {
        if (powered && !wasPowered && this.level != null) {
            this.startTime = this.level.getGameTime();
            this.pausedOffset = 0;
            this.playing = true;
            updatePowerState();
            updateAndSync();
        }
        this.wasPowered = powered;
    }

    private void updateAndSync() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    private void updatePowerState() {
        if (this.level == null || this.level.isClientSide())
            return;
        BlockState state = getBlockState();
        if (state.hasProperty(RadioBlock.POWERED)) {
            boolean wasPowered = state.getValue(RadioBlock.POWERED);
            if (wasPowered != playing) {
                this.level.setBlock(getBlockPos(), state.setValue(RadioBlock.POWERED, playing), 3);
                this.level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.createTag());
        tag.putLong("StartTime", startTime);
        tag.putFloat("Volume", volume);
        tag.putBoolean("Looping", looping);
        tag.putBoolean("WasPowered", wasPowered);
        tag.putBoolean("Playing", playing);
        tag.putLong("PausedOffset", pausedOffset);
        tag.putLong("InsertTime", insertTime);
        tag.putLong("RemoveTime", removeTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.fromTag(tag.getList("Inventory", 10));
        startTime = tag.getLong("StartTime");
        if (startTime > 1000000000000L) {
            startTime = 0;
            playing = false;
        }
        volume = tag.contains("Volume") ? tag.getFloat("Volume") : 0.75f;
        looping = tag.getBoolean("Looping");
        wasPowered = tag.getBoolean("WasPowered");
        playing = tag.getBoolean("Playing");
        pausedOffset = tag.getLong("PausedOffset");
        insertTime = tag.getLong("InsertTime");
        removeTime = tag.getLong("RemoveTime");
        this.wasEmpty = inventory.isEmpty();
    }

    public ItemStack getCassette() {
        return inventory.getItem(0);
    }

    public long getStartTime() {
        if (!playing)
            return 0;
        return startTime;
    }

    public float getVolume() {
        return volume;
    }

    public boolean isLooping() {
        return looping;
    }

    public boolean isPlaying() {
        return playing;
    }

    public long getInsertTime() {
        return insertTime;
    }

    public long getRemoveTime() {
        return removeTime;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.analogaudio.radio");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new RadioMenu(id, inv, getBlockPos());
    }

    @Nullable
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

    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RadioBlockEntity blockEntity) {
        if (level.isClientSide()) {
            if (blockEntity.playing) {
                ItemStack cassette = blockEntity.getCassette();
                CassetteData data = CassetteData.get(cassette);
                if (data != null && !data.url().isEmpty()) {
                    Vec3 vecPos = Vec3.atCenterOf(pos);
                    ClientAudioEngine.tickRadio(
                            pos,
                            vecPos,
                            data,
                            blockEntity.startTime,
                            blockEntity.volume,
                            blockEntity.looping);
                } else {
                    ClientAudioEngine.stopRadio(pos);
                }
            } else {
                ClientAudioEngine.stopRadio(pos);
            }
        }
    }

    @Override
    public void setRemoved() {
        if (this.level != null && this.level.isClientSide()) {
            ClientAudioEngine.stopRadio(this.worldPosition);
        }
        super.setRemoved();
    }
}
