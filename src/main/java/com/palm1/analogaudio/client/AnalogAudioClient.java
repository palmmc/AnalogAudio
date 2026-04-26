package com.palm1.analogaudio.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.client.gui.CassetteDeckScreen;
import com.palm1.analogaudio.client.gui.RadioScreen;
import com.palm1.analogaudio.registry.ModBlockEntities;
import com.palm1.analogaudio.registry.ModItems;
import com.palm1.analogaudio.registry.ModMenus;
import com.palm1.analogaudio.client.render.RadioBlockRenderer;
import com.palm1.analogaudio.client.render.CassetteDeckBlockRenderer;
import com.palm1.analogaudio.client.render.SpeakerBlockRenderer;
import com.palm1.analogaudio.item.CassetteData;

public class AnalogAudioClient {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(AnalogAudioClient::onClientSetup);
        modEventBus.addListener(AnalogAudioClient::onRegisterRenderers);
        modEventBus.addListener(AnalogAudioClient::registerItemColors);
        modEventBus.addListener(AnalogAudioClient::registerModels);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.CASSETTE_DECK_MENU.get(), CassetteDeckScreen::new);
            MenuScreens.register(ModMenus.RADIO_MENU.get(), RadioScreen::new);
        });
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO.get(), RadioBlockRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CASSETTE_DECK.get(), CassetteDeckBlockRenderer::new);
        if (ModBlockEntities.SPEAKER != null)
            event.registerBlockEntityRenderer(ModBlockEntities.SPEAKER.get(), SpeakerBlockRenderer::new);
    }

    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0) {
                CassetteData data = CassetteData.get(stack);
                if (data != null) {
                    return 0xFF000000 | data.color();
                }
                return 0xFFFFFFFF;
            }
            return -1;
        }, ModItems.CASSETTE_TAPE.get());
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(new ResourceLocation(AnalogAudio.MODID, "item/cassette_tape_3d"));
        if (net.minecraftforge.fml.ModList.get().isLoaded("voicechat")) {
            event.register(com.palm1.analogaudio.client.render.WalkieTalkieRenderer.BODY_MODEL);
            event.register(com.palm1.analogaudio.client.render.WalkieTalkieRenderer.BUTTON_MODEL);
        }
    }
}
