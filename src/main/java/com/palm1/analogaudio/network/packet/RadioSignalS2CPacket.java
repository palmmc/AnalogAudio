package com.palm1.analogaudio.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record RadioSignalS2CPacket(UUID senderUuid, Vec3 position, int frequency, boolean isSpeaker) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(senderUuid);
        buffer.writeDouble(position.x);
        buffer.writeDouble(position.y);
        buffer.writeDouble(position.z);
        buffer.writeInt(frequency);
        buffer.writeBoolean(isSpeaker);
    }

    public static RadioSignalS2CPacket decode(FriendlyByteBuf buffer) {
        return new RadioSignalS2CPacket(
                buffer.readUUID(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readInt(),
                buffer.readBoolean()
        );
    }

    public static void handle(RadioSignalS2CPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            com.palm1.analogaudio.network.ClientPacketHandlers.handleRadioSignal(message);
        });
        context.setPacketHandled(true);
    }
}
