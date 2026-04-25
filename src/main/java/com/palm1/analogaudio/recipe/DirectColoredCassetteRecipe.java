package com.palm1.analogaudio.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.Level;

import java.util.UUID;

import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.registry.ModItems;
import com.palm1.analogaudio.registry.ModRecipeSerializers;

public class DirectColoredCassetteRecipe extends CustomRecipe {

    public DirectColoredCassetteRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        boolean hasKelp = false;
        boolean hasRedstone = false;
        boolean hasGold = false;
        boolean hasDye = false;
        int count = 0;

        for (int i = 0; i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                count++;
                if (stack.is(Items.DRIED_KELP))
                    hasKelp = true;
                else if (stack.is(Items.REDSTONE))
                    hasRedstone = true;
                else if (stack.is(Items.GOLD_INGOT))
                    hasGold = true;
                else if (stack.getItem() instanceof DyeItem) {
                    if (hasDye)
                        return false;
                    hasDye = true;
                } else {
                    return false;
                }
            }
        }

        return hasKelp && hasRedstone && hasGold && hasDye && count == 4;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registryAccess) {
        int color = 0xFFFFFF;

        for (int i = 0; i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof DyeItem dyeItem) {
                color = dyeItem.getDyeColor().getFireworkColor();
                break;
            }
        }

        ItemStack result = new ItemStack(ModItems.CASSETTE_TAPE.get());
        CassetteData data = new CassetteData(UUID.randomUUID().toString(), "", "", 0xFF000000 | color);
        CassetteData.set(result, data);

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DIRECT_COLORED_CASSETTE.get();
    }
}
