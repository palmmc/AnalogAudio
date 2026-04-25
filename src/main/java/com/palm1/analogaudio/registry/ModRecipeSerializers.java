package com.palm1.analogaudio.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.recipe.DirectColoredCassetteRecipe;
import com.palm1.analogaudio.recipe.DyeCassetteRecipe;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, AnalogAudio.MODID);

    public static final RegistryObject<RecipeSerializer<?>> DIRECT_COLORED_CASSETTE = SERIALIZERS.register(
            "direct_colored_cassette",
            () -> new SimpleCraftingRecipeSerializer<>(DirectColoredCassetteRecipe::new));

    public static final RegistryObject<RecipeSerializer<?>> DYE_CASSETTE = SERIALIZERS.register("dye_cassette",
            () -> new SimpleCraftingRecipeSerializer<>(DyeCassetteRecipe::new));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
