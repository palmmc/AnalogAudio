package com.palm1.analogaudio.network.packet;

import com.palm1.analogaudio.block.entity.SpeakerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetFrequencyC2SPacket(BlockPos pos, int frequency) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeInt(frequency);
    }

    public static SetFrequencyC2SPacket decode(FriendlyByteBuf buffer) {
        return new SetFrequencyC2SPacket(buffer.readBlockPos(), buffer.readInt());
    }

    public static void handle(SetFrequencyC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) return;
            BlockEntity be = context.getSender().level().getBlockEntity(message.pos());
            if (be instanceof SpeakerBlockEntity speaker) {
                speaker.setFrequency(message.frequency());
            }
        });
        context.setPacketHandled(true);
    }
}
