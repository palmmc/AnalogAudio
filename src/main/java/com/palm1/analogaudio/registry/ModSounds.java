package com.palm1.analogaudio.registry;

import com.palm1.analogaudio.AnalogAudio;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
        public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
                        AnalogAudio.MODID);

        public static final RegistryObject<SoundEvent> WRITE = registerSoundEvent("write");
        public static final RegistryObject<SoundEvent> ERASE = registerSoundEvent("erase");
        public static final RegistryObject<SoundEvent> WALKIE_PRESS = registerSoundEvent("walkie_press");
        public static final RegistryObject<SoundEvent> WALKIE_UNPRESS = registerSoundEvent("walkie_unpress");
        public static final RegistryObject<SoundEvent> FREQUENCY_TICK = registerSoundEvent("frequency_tick");
        public static final RegistryObject<SoundEvent> FREQUENCY_SELECT = registerSoundEvent("frequency_select");
        public static final RegistryObject<SoundEvent> CASSETTE_INSERT = registerSoundEvent("cassette_insert");
        public static final RegistryObject<SoundEvent> CASSETTE_EJECT = registerSoundEvent("cassette_eject");
        public static final RegistryObject<SoundEvent> SWITCH_ON = registerSoundEvent("switch_on");
        public static final RegistryObject<SoundEvent> SWITCH_OFF = registerSoundEvent("switch_off");

        private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
                return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(AnalogAudio.MODID, name)));
        }

        public static void register(IEventBus eventBus) {
                SOUNDS.register(eventBus);
        }
}
