package com.palm1.analogaudio.network.packet;

import com.palm1.analogaudio.item.CassetteData;
import com.palm1.analogaudio.inventory.CassetteDeckMenu;
import com.palm1.analogaudio.registry.ModItems;
import com.palm1.analogaudio.network.AnalogAudioNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record WriteCassetteC2SPacket(String url, String name, int color) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(url);
        buffer.writeUtf(name);
        buffer.writeInt(color);
    }

    public static WriteCassetteC2SPacket decode(FriendlyByteBuf buffer) {
        return new WriteCassetteC2SPacket(buffer.readUtf(), buffer.readUtf(), buffer.readInt());
    }

    public static void handle(WriteCassetteC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;

            if (!message.url().isEmpty() && !isValidUrl(message.url())) {
                AnalogAudioNetwork.CHANNEL.reply(new WriteResultS2CPacket(4), context);
                return;
            }

            if (player.containerMenu instanceof CassetteDeckMenu deckMenu) {
                ItemStack cassette = deckMenu.getInventory().getItem(0);
                if (!cassette.isEmpty() && cassette.is(ModItems.CASSETTE_TAPE.get())) {
                    CassetteData oldData = CassetteData.get(cassette);

                    String uuid = oldData != null ? oldData.uuid() : UUID.randomUUID().toString();
                    String url = message.url().isEmpty() && oldData != null ? oldData.url() : message.url();
                    String name = message.name().isEmpty() && oldData != null ? oldData.name() : message.name();

                    CassetteData.set(cassette, new CassetteData(uuid, url, name, message.color()));
                    deckMenu.getInventory().setChanged();
                    AnalogAudioNetwork.CHANNEL.reply(new WriteResultS2CPacket(0), context);
                } else {
                    AnalogAudioNetwork.CHANNEL.reply(new WriteResultS2CPacket(1), context);
                }
            }
        });
        context.setPacketHandled(true);
    }

    private static boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
