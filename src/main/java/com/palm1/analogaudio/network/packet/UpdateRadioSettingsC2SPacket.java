package com.palm1.analogaudio.network.packet;

import com.palm1.analogaudio.block.entity.RadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateRadioSettingsC2SPacket(BlockPos pos, float volume, boolean looping, boolean playing) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeFloat(volume);
        buffer.writeBoolean(looping);
        buffer.writeBoolean(playing);
    }

    public static UpdateRadioSettingsC2SPacket decode(FriendlyByteBuf buffer) {
        return new UpdateRadioSettingsC2SPacket(buffer.readBlockPos(), buffer.readFloat(), buffer.readBoolean(), buffer.readBoolean());
    }

    public static void handle(UpdateRadioSettingsC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) return;
            BlockEntity be = context.getSender().level().getBlockEntity(message.pos());
            if (be instanceof RadioBlockEntity radio) {
                radio.setSettings(message.volume(), message.looping(), message.playing());
            }
        });
        context.setPacketHandled(true);
    }
}
