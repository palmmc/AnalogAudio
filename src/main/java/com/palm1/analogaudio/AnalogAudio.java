package com.palm1.analogaudio;

import com.mojang.logging.LogUtils;
import com.palm1.analogaudio.network.AnalogAudioNetwork;
import com.palm1.analogaudio.registry.*;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(AnalogAudio.MODID)
public class AnalogAudio {
    public static final String MODID = "analogaudio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AnalogAudio() {
        IEventBus modEventBus = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.SERVER,
                com.palm1.analogaudio.config.ModConfig.SERVER_SPEC);
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.CLIENT,
                com.palm1.analogaudio.config.ModConfig.CLIENT_SPEC);

        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);
        ModSounds.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModRecipeSerializers.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.palm1.analogaudio.client.AnalogAudioClient.register(modEventBus);
            ModLoadingContext.get().registerExtensionPoint(
                    net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory(
                            (client, parent) -> new com.palm1.analogaudio.client.gui.ConfigScreen(parent)));
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AnalogAudioNetwork::register);
    }
}
