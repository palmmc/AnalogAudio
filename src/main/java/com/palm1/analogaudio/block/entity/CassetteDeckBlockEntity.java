package com.palm1.analogaudio.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import com.palm1.analogaudio.inventory.CassetteDeckMenu;
import com.palm1.analogaudio.registry.ModBlockEntities;
import com.palm1.analogaudio.registry.ModSounds;
import com.palm1.analogaudio.registry.ModItems;

public class CassetteDeckBlockEntity extends BlockEntity implements MenuProvider {
    public final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return stack.is(ModItems.CASSETTE_TAPE.get());
        }
    };
    private boolean wasEmpty = true;
    private ItemStack lastCassette = ItemStack.EMPTY;
    private long insertTime = 0;
    private long removeTime = 0;

    public CassetteDeckBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CASSETTE_DECK.get(), pos, blockState);

        this.wasEmpty = inventory.isEmpty();

        inventory.addListener(container -> {
            ItemStack currentCassette = container.getItem(0);
            boolean isEmpty = currentCassette.isEmpty();
            boolean itemChanged = !ItemStack.matches(currentCassette, lastCassette);

            if (this.level != null && !this.level.isClientSide()) {
                if (itemChanged) {
                    if (this.wasEmpty && !isEmpty) {
                        this.insertTime = this.level.getGameTime();
                        this.level.playSound(null, this.worldPosition, ModSounds.CASSETTE_INSERT.get(),
                                SoundSource.BLOCKS,
                                1.0f, 1.0f);
                    } else if (!this.wasEmpty && isEmpty) {
                        this.removeTime = this.level.getGameTime();
                        this.level.playSound(null, this.worldPosition, ModSounds.CASSETTE_EJECT.get(),
                                SoundSource.BLOCKS,
                                1.0f, 1.0f);
                    }
                    setChanged();
                    updateAndSync();
                }
            }
            this.lastCassette = currentCassette.copy();
            this.wasEmpty = isEmpty;
        });
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.analogaudio.cassette_deck");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CassetteDeckMenu(containerId, playerInventory, this);
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

    private void updateAndSync() {
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.createTag());
        tag.putLong("InsertTime", insertTime);
        tag.putLong("RemoveTime", removeTime);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inventory.fromTag(tag.getList("Inventory", 10));
        insertTime = tag.getLong("InsertTime");
        removeTime = tag.getLong("RemoveTime");
        this.wasEmpty = inventory.isEmpty();
    }

    public ItemStack getCassette() {
        return inventory.getItem(0);
    }

    public long getInsertTime() {
        return insertTime;
    }

    public long getRemoveTime() {
        return removeTime;
    }

    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CassetteDeckBlockEntity blockEntity) {
    }
}
