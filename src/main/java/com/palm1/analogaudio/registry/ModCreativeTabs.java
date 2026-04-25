package com.palm1.analogaudio.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister
            .create(Registries.CREATIVE_MODE_TAB, AnalogAudio.MODID);

    public static final RegistryObject<CreativeModeTab> ANALOG_AUDIO_TAB = CREATIVE_MODE_TABS.register("analogaudio_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.RADIO.get()))
                    .title(Component.translatable("itemGroup.analogaudio.analogaudio_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.RADIO.get());
                        output.accept(ModItems.CASSETTE_DECK.get());
                        output.accept(ModItems.SPEAKER.get());
                        output.accept(ModItems.CASSETTE_TAPE.get());
                        output.accept(ModItems.WALKIE_TALKIE.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
