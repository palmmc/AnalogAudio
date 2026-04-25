package com.palm1.analogaudio.network.packet;

import com.palm1.analogaudio.inventory.CassetteDeckMenu;
import com.palm1.analogaudio.registry.ModItems;
import com.palm1.analogaudio.network.AnalogAudioNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record EraseCassetteC2SPacket() {
    public void encode(FriendlyByteBuf buffer) {
    }

    public static EraseCassetteC2SPacket decode(FriendlyByteBuf buffer) {
        return new EraseCassetteC2SPacket();
    }

    public static void handle(EraseCassetteC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;
            if (player.containerMenu instanceof CassetteDeckMenu deckMenu) {
                ItemStack cassette = deckMenu.getInventory().getItem(0);
                if (!cassette.isEmpty() && cassette.is(ModItems.CASSETTE_TAPE.get())) {
                    if (cassette.hasTag()) {
                        cassette.getTag().remove("CassetteData");
                    }
                    deckMenu.getInventory().setChanged();
                    AnalogAudioNetwork.CHANNEL.reply(new WriteResultS2CPacket(2), context);
                } else {
                    AnalogAudioNetwork.CHANNEL.reply(new WriteResultS2CPacket(3), context);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
