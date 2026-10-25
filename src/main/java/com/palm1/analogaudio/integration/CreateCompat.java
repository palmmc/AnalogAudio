package com.palm1.analogaudio.integration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.integration.create.RadioArmInteractionPoint;
import com.palm1.analogaudio.registry.ModBlocks;

public class CreateCompat {
    public static void init(IEventBus modEventBus) {
        if (ModList.get().isLoaded("create")) {
            modEventBus.addListener(CreateCompat::onRegister);
            modEventBus.addListener(CreateCompat::onCommonSetup);
            try {
                Class.forName("com.palm1.analogaudio.integration.create.CreateDisplaySources")
                        .getMethod("register", IEventBus.class)
                        .invoke(null, modEventBus);
            } catch (Exception e) {
                AnalogAudio.LOGGER.error("Failed to register display sources for Create", e);
            }
        }
    }

    private static void onRegister(RegisterEvent event) {
        try {
            Class<?> registriesClass = Class.forName("com.simibubi.create.api.registry.CreateRegistries");
            Field armRegistryKeyField = registriesClass.getField("ARM_INTERACTION_POINT_TYPE");
            net.minecraft.resources.ResourceKey<Registry<Object>> armRegistryKey = (net.minecraft.resources.ResourceKey<Registry<Object>>) armRegistryKeyField
                    .get(null);

            if (event.getRegistryKey().equals(armRegistryKey)) {
                event.register(armRegistryKey, ResourceLocation.fromNamespaceAndPath(AnalogAudio.MODID, "radio"),
                        () -> new RadioArmInteractionPoint.Type());
                AnalogAudio.LOGGER.info("Registered radio arm interaction point for Create");
            }
        } catch (Exception e) {
        }
    }

    private static void onCommonSetup(final net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Class<?> movementBehaviourClass = Class
                        .forName("com.simibubi.create.api.behaviour.movement.MovementBehaviour");
                Field registryField = movementBehaviourClass.getField("REGISTRY");
                Object registry = registryField.get(null);
                // SimpleRegistry<K, V>.register(K, V) is generic -> erased to register(Object, Object).
                // Look it up on the public API interface (the runtime class is package-private).
                Method addMethod = Class.forName("com.simibubi.create.api.registry.SimpleRegistry")
                        .getMethod("register", Object.class, Object.class);

                Object behavior = Class.forName("com.palm1.analogaudio.integration.create.RadioMovementBehaviour")
                        .getDeclaredConstructor().newInstance();
                addMethod.invoke(registry, ModBlocks.RADIO.get(), behavior);

            } catch (Exception e) {
                AnalogAudio.LOGGER.error("Failed to register Create movement behavior", e);
            }
        });
    }
}
