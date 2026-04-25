package com.palm1.analogaudio.network;

import com.palm1.analogaudio.AnalogAudio;
import com.palm1.analogaudio.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AnalogAudioNetwork {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AnalogAudio.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++, WriteCassetteC2SPacket.class, WriteCassetteC2SPacket::encode,
                WriteCassetteC2SPacket::decode, WriteCassetteC2SPacket::handle);
        CHANNEL.registerMessage(packetId++, EraseCassetteC2SPacket.class, EraseCassetteC2SPacket::encode,
                EraseCassetteC2SPacket::decode, EraseCassetteC2SPacket::handle);
        CHANNEL.registerMessage(packetId++, UpdateRadioSettingsC2SPacket.class, UpdateRadioSettingsC2SPacket::encode,
                UpdateRadioSettingsC2SPacket::decode, UpdateRadioSettingsC2SPacket::handle);
        CHANNEL.registerMessage(packetId++, WriteResultS2CPacket.class, WriteResultS2CPacket::encode,
                WriteResultS2CPacket::decode, WriteResultS2CPacket::handle);
        CHANNEL.registerMessage(packetId++, RadioSignalS2CPacket.class, RadioSignalS2CPacket::encode,
                RadioSignalS2CPacket::decode, RadioSignalS2CPacket::handle);
        CHANNEL.registerMessage(packetId++, SetFrequencyC2SPacket.class, SetFrequencyC2SPacket::encode,
                SetFrequencyC2SPacket::decode, SetFrequencyC2SPacket::handle);
        CHANNEL.registerMessage(packetId++, SetWalkieFrequencyC2SPacket.class, SetWalkieFrequencyC2SPacket::encode,
                SetWalkieFrequencyC2SPacket::decode, SetWalkieFrequencyC2SPacket::handle);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        CHANNEL.sendTo(message, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
}
