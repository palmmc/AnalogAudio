package com.palm1.analogaudio.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class CassetteTapeItem extends Item {
    public CassetteTapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        var data = CassetteData.get(stack);
        if (data != null && !data.name().isEmpty()) {
            int insertIndex = Math.min(tooltip.size(), 1);
            tooltip.add(insertIndex, Component.literal(data.name()).withStyle(ChatFormatting.GRAY));
        }
    }
}
