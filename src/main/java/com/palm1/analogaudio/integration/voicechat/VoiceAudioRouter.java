package com.palm1.analogaudio.integration.voicechat;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.StaticSoundPacket;
import de.maxhenkel.voicechat.api.Position;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.UUID;

import com.palm1.analogaudio.config.ModConfig;
import com.palm1.analogaudio.item.WalkieTalkieItem;
import com.palm1.analogaudio.network.AnalogAudioNetwork;
import com.palm1.analogaudio.network.packet.RadioSignalS2CPacket;

public class VoiceAudioRouter {

    public static void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (senderConnection == null)
            return;

        ServerPlayer sender = senderConnection.getPlayer();
        if (sender == null)
            return;

        net.minecraft.server.level.ServerPlayer actualPlayer = (net.minecraft.server.level.ServerPlayer) sender
                .getPlayer();
        ItemStack walkieTalkie = null;

        if (actualPlayer.getMainHandItem().getItem() instanceof WalkieTalkieItem) {
            walkieTalkie = actualPlayer.getMainHandItem();
        } else if (actualPlayer.getOffhandItem().getItem() instanceof WalkieTalkieItem) {
            walkieTalkie = actualPlayer.getOffhandItem();
        }

        if (walkieTalkie == null) {
            return;
        }

        int activeFrequency = WalkieTalkieItem.getFrequency(walkieTalkie);
        @SuppressWarnings("unused")
        byte[] opusData = event.getPacket().getOpusEncodedData();
        UUID channelId = sender.getUuid();

        StaticSoundPacket staticPacket = event.getPacket().staticSoundPacketBuilder()
                .channelId(channelId)
                .category("walkie_talkies")
                .build();

        for (net.minecraft.server.level.ServerPlayer recipient : actualPlayer.server.getPlayerList().getPlayers()) {
            if (recipient == actualPlayer)
                continue;

            boolean hasWalkie = false;
            for (ItemStack itemStack : recipient.getInventory().items) {
                if (itemStack.getItem() instanceof WalkieTalkieItem
                        && WalkieTalkieItem.getFrequency(itemStack) == activeFrequency) {
                    hasWalkie = true;
                    break;
                }
            }
            if (!hasWalkie) {
                for (ItemStack itemStack : recipient.getInventory().offhand) {
                    if (itemStack.getItem() instanceof WalkieTalkieItem
                            && WalkieTalkieItem.getFrequency(itemStack) == activeFrequency) {
                        hasWalkie = true;
                        break;
                    }
                }
            }

            if (hasWalkie) {
                VoicechatApiHandle.ifServerPresent(serverApi -> {
                    VoicechatConnection recipientConn = serverApi.getConnectionOf(recipient.getUUID());
                    if (recipientConn != null) {
                        if (ModConfig.SERVER_CONFIG.enableWalkieFiltering.get()) {
                            AnalogAudioNetwork.sendToPlayer(
                                    new RadioSignalS2CPacket(
                                            channelId, actualPlayer.position(), activeFrequency, false),
                                    recipient);
                        }

                        serverApi.sendStaticSoundPacketTo(recipientConn, staticPacket);
                    }
                });
            }
        }

        Set<SpeakerInstance> speakers = SpeakerManager.getSpeakersOnFrequency(activeFrequency);
        for (SpeakerInstance speaker : speakers) {
            if (speaker.getLevel() == null) {
                continue;
            }

            Vec3 speakerPos = speaker.getPosition();
            VoicechatApiHandle.ifServerPresent(serverApi -> {
                Position position = serverApi.createPosition(
                        speakerPos.x(),
                        speakerPos.y(),
                        speakerPos.z());

                UUID sessionChannelId = UUID
                        .nameUUIDFromBytes(
                                ("speaker" + speaker.getIdentity().toString() + sender.getUuid()).getBytes());

                LocationalSoundPacket locationalPacket = event.getPacket().locationalSoundPacketBuilder()
                        .channelId(sessionChannelId)
                        .category("speaker_blocks")
                        .distance(64.0f)
                        .position(position)
                        .build();

                MinecraftServer server = speaker.getLevel().getServer();
                if (server == null)
                    return;

                for (net.minecraft.server.level.ServerPlayer recipient : server.getPlayerList().getPlayers()) {
                    Vec3 recipientGlobalPos = recipient.position();
                    double distSq = recipientGlobalPos.distanceToSqr(speakerPos);

                    if (distSq > 64 * 64)
                        continue;

                    VoicechatConnection targetConn = serverApi.getConnectionOf(recipient.getUUID());
                    if (targetConn != null) {
                        if (ModConfig.SERVER_CONFIG.enableWalkieFiltering.get()) {
                            AnalogAudioNetwork.sendToPlayer(
                                    new RadioSignalS2CPacket(
                                            sessionChannelId, speakerPos,
                                            activeFrequency, true),
                                    recipient);
                        }

                        serverApi.sendLocationalSoundPacketTo(targetConn, locationalPacket);
                    }
                }
                speaker.onVoicePacketReceived();
            });
        }
    }
}
