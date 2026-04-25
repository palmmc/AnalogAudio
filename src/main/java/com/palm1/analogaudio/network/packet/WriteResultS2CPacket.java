package com.palm1.analogaudio.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record WriteResultS2CPacket(int statusType) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(statusType);
    }

    public static WriteResultS2CPacket decode(FriendlyByteBuf buffer) {
        return new WriteResultS2CPacket(buffer.readInt());
    }

    public static void handle(WriteResultS2CPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.palm1.analogaudio.network.ClientPacketHandlers.handleWriteResult(message);
        });
        context.setPacketHandled(true);
    }
}
