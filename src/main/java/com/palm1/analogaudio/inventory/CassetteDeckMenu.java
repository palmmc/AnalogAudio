package com.palm1.analogaudio.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

import com.palm1.analogaudio.block.entity.CassetteDeckBlockEntity;
import com.palm1.analogaudio.registry.ModMenus;

public class CassetteDeckMenu extends AbstractContainerMenu {
    private final SimpleContainer inventory;

    public CassetteDeckMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(1));
    }

    public CassetteDeckMenu(int containerId, Inventory playerInventory, CassetteDeckBlockEntity blockEntity) {
        this(containerId, playerInventory, blockEntity.inventory);
    }

    public CassetteDeckMenu(int containerId, Inventory playerInventory, SimpleContainer inventory) {
        super(ModMenus.CASSETTE_DECK_MENU.get(), containerId);
        this.inventory = inventory;
        this.inventory.startOpen(playerInventory.player);

        this.addSlot(new CassetteSlot(inventory, 0, 83, 65));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 90 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 148));
        }
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.inventory.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.inventory.stopOpen(player);
    }
}
