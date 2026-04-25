package com.palm1.analogaudio.network.packet;

import com.palm1.analogaudio.item.WalkieTalkieItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SetWalkieFrequencyC2SPacket(InteractionHand hand, int frequency) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeEnum(hand);
        buffer.writeInt(frequency);
    }

    public static SetWalkieFrequencyC2SPacket decode(FriendlyByteBuf buffer) {
        return new SetWalkieFrequencyC2SPacket(buffer.readEnum(InteractionHand.class), buffer.readInt());
    }

    public static void handle(SetWalkieFrequencyC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (context.getSender() == null) return;
            ItemStack stack = context.getSender().getItemInHand(message.hand());
            if (stack.getItem() instanceof WalkieTalkieItem) {
                WalkieTalkieItem.setFrequency(stack, message.frequency());
            }
        });
        context.setPacketHandled(true);
    }
}
