package com.palm1.analogaudio.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ModConfig {
    public static class Server {
        public final ForgeConfigSpec.BooleanValue enableWalkieFiltering;
        public final ForgeConfigSpec.BooleanValue enableSpeakerFiltering;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.push("filtering");
            enableWalkieFiltering = builder.comment("Whether to enable walkie-talkie audio filtering (static, etc)")
                    .define("enable_walkie_filtering", true);
            enableSpeakerFiltering = builder.comment("Whether to enable speaker audio filtering (static, etc)")
                    .define("enable_speaker_filtering", true);
            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.BooleanValue showWalkieHud;
        public final ForgeConfigSpec.BooleanValue enableCassetteAnimation;
        public final ForgeConfigSpec.BooleanValue enableSpeakerAnimation;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("ui");
            showWalkieHud = builder.comment("Whether to show the walkie-talkie frequency HUD").define("show_walkie_hud", true);
            builder.pop();

            builder.push("animation");
            enableCassetteAnimation = builder.comment("Whether to enable cassette deck animations").define("enable_cassette_animation", true);
            enableSpeakerAnimation = builder.comment("Whether to enable speaker block animations").define("enable_speaker_animation", true);
            builder.pop();
        }
    }

    public static final Server SERVER_CONFIG;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_CONFIG = specPair.getLeft();
        SERVER_SPEC = specPair.getRight();
    }

    public static final Client CLIENT_CONFIG;
    public static final ForgeConfigSpec CLIENT_SPEC;

    static {
        final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_CONFIG = specPair.getLeft();
        CLIENT_SPEC = specPair.getRight();
    }
}
