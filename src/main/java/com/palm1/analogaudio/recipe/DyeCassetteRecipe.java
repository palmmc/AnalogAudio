package com.palm1.analogaudio.recipe;

import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.registry.ModItems;
import com.palm1.analogaudio.registry.ModRecipeSerializers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.level.Level;

public class DyeCassetteRecipe extends CustomRecipe {

    public DyeCassetteRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer input, Level level) {
        int cassettes = 0;
        int dyes = 0;

        for (int i = 0; i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.CASSETTE_TAPE.get())) {
                    cassettes++;
                } else if (stack.getItem() instanceof DyeItem) {
                    dyes++;
                } else {
                    return false;
                }
            }
        }

        return cassettes == 1 && dyes >= 1;
    }

    @Override
    public ItemStack assemble(CraftingContainer input, net.minecraft.core.RegistryAccess registryAccess) {
        int[] rgbSum = new int[3];
        int maxIntensity = 0;
        int dyeCount = 0;
        ItemStack cassette = ItemStack.EMPTY;

        for (int i = 0; i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(ModItems.CASSETTE_TAPE.get())) {
                    cassette = stack;
                } else if (stack.getItem() instanceof DyeItem dyeItem) {
                    int colorInt = dyeItem.getDyeColor().getFireworkColor();
                    int r = (colorInt >> 16) & 255;
                    int g = (colorInt >> 8) & 255;
                    int b = colorInt & 255;
                    maxIntensity += Math.max(r, Math.max(g, b));
                    rgbSum[0] += r;
                    rgbSum[1] += g;
                    rgbSum[2] += b;
                    dyeCount++;
                }
            }
        }

        if (cassette.isEmpty() || dyeCount == 0) {
            return ItemStack.EMPTY;
        }

        CassetteData oldData = CassetteData.get(cassette);
        int oldColor = 0xFFFFFF;
        if (oldData != null && oldData.color() != 0xFFFFFF && oldData.color() != 0) {
            oldColor = oldData.color();
            int r = (oldColor >> 16) & 255;
            int g = (oldColor >> 8) & 255;
            int b = oldColor & 255;
            maxIntensity += Math.max(r, Math.max(g, b));
            rgbSum[0] += r;
            rgbSum[1] += g;
            rgbSum[2] += b;
            dyeCount++;
        }

        int avgR = rgbSum[0] / dyeCount;
        int avgG = rgbSum[1] / dyeCount;
        int avgB = rgbSum[2] / dyeCount;
        float avgIntensity = (float) maxIntensity / (float) dyeCount;
        float maxAvg = (float) Math.max(avgR, Math.max(avgG, avgB));
        avgR = (int) ((float) avgR * avgIntensity / maxAvg);
        avgG = (int) ((float) avgG * avgIntensity / maxAvg);
        avgB = (int) ((float) avgB * avgIntensity / maxAvg);
        int finalColor = 0xFF000000 | (avgR << 16) | (avgG << 8) | avgB;

        ItemStack result = cassette.copy();
        if (oldData != null) {
            CassetteData.set(result, new CassetteData(oldData.uuid(), oldData.url(), oldData.name(), finalColor));
        } else {
            CassetteData.set(result, new CassetteData("", "", "", finalColor));
        }

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.DYE_CASSETTE.get();
    }
}
