package com.palm1.analogaudio.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.block.CassetteDeckBlock;
import com.palm1.analogaudio.block.RadioBlock;
import com.palm1.analogaudio.block.SpeakerBlock;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            AnalogAudio.MODID);

    public static final RegistryObject<Block> RADIO = BLOCKS.register("radio",
            () -> new RadioBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)));

    public static final RegistryObject<Block> CASSETTE_DECK = BLOCKS.register("cassette_deck",
            () -> new CassetteDeckBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)));

    public static final RegistryObject<Block> SPEAKER = BLOCKS.register("speaker",
            () -> new SpeakerBlock(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD)));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
