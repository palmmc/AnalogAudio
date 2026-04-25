package com.palm1.analogaudio.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public record CassetteData(String uuid, String url, String name, int color) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("uuid", uuid);
        tag.putString("url", url);
        tag.putString("name", name);
        tag.putInt("color", color);
        return tag;
    }

    public static CassetteData load(CompoundTag tag) {
        return new CassetteData(
            tag.getString("uuid"),
            tag.getString("url"),
            tag.getString("name"),
            tag.getInt("color")
        );
    }

    @Nullable
    public static CassetteData get(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("CassetteData")) {
            return load(stack.getTag().getCompound("CassetteData"));
        }
        return null;
    }

    public static void set(ItemStack stack, CassetteData data) {
        stack.getOrCreateTag().put("CassetteData", data.save());
    }
}
