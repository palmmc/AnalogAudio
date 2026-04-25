package com.palm1.analogaudio.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.inventory.CassetteDeckMenu;
import com.palm1.analogaudio.inventory.RadioMenu;

public class ModMenus {
        public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES,
                        AnalogAudio.MODID);

        public static final RegistryObject<MenuType<CassetteDeckMenu>> CASSETTE_DECK_MENU = MENUS.register(
                        "cassette_deck_menu",
                        () -> IForgeMenuType.create(CassetteDeckMenu::new));

        public static final RegistryObject<MenuType<RadioMenu>> RADIO_MENU = MENUS.register("radio_menu",
                        () -> IForgeMenuType.create(RadioMenu::new));

        public static void register(IEventBus eventBus) {
                MENUS.register(eventBus);
        }
}
