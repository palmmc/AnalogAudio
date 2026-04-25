package com.palm1.analogaudio.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.block.entity.CassetteDeckBlockEntity;
import com.palm1.analogaudio.block.entity.RadioBlockEntity;
import com.palm1.analogaudio.block.entity.SpeakerBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, AnalogAudio.MODID);

    public static final RegistryObject<BlockEntityType<CassetteDeckBlockEntity>> CASSETTE_DECK = BLOCK_ENTITIES
            .register("cassette_deck",
                    () -> BlockEntityType.Builder.of(CassetteDeckBlockEntity::new, ModBlocks.CASSETTE_DECK.get()).build(null));

    public static final RegistryObject<BlockEntityType<RadioBlockEntity>> RADIO = BLOCK_ENTITIES.register("radio",
            () -> BlockEntityType.Builder.of(RadioBlockEntity::new, ModBlocks.RADIO.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpeakerBlockEntity>> SPEAKER = BLOCK_ENTITIES
            .register("speaker",
                    () -> BlockEntityType.Builder.of(SpeakerBlockEntity::new, ModBlocks.SPEAKER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
