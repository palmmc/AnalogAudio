package com.palm1.analogaudio.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.item.CassetteTapeItem;
import com.palm1.analogaudio.item.WalkieTalkieItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            AnalogAudio.MODID);

    public static final RegistryObject<Item> RADIO = ITEMS.register("radio",
            () -> new BlockItem(ModBlocks.RADIO.get(), new Item.Properties()));

    public static final RegistryObject<Item> CASSETTE_DECK = ITEMS.register("cassette_deck",
            () -> new BlockItem(ModBlocks.CASSETTE_DECK.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPEAKER = ITEMS.register("speaker",
            () -> new BlockItem(ModBlocks.SPEAKER.get(), new Item.Properties()));

    public static final RegistryObject<Item> CASSETTE_TAPE = ITEMS.register("cassette_tape",
            () -> new CassetteTapeItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> WALKIE_TALKIE = ITEMS.register("walkie_talkie",
            () -> new WalkieTalkieItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
