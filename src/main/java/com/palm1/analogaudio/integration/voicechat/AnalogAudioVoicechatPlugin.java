package com.palm1.analogaudio.integration.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.VolumeCategory;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;

import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;

import com.palm1.analogaudio.AnalogAudio;

@ForgeVoicechatPlugin
public class AnalogAudioVoicechatPlugin implements VoicechatPlugin {

    public AnalogAudioVoicechatPlugin() {
        AnalogAudio.LOGGER.info("Voicechat Plugin INSTANCE CREATED.");
    }

    @Override
    public String getPluginId() {
        return AnalogAudio.MODID;
    }

    @Override
    public void initialize(VoicechatApi api) {
        VoicechatApiHandle.setApi(api);
        if (api instanceof de.maxhenkel.voicechat.api.VoicechatClientApi clientApi) {
            VoicechatApiHandle.setClientApi(clientApi);
            AnalogAudio.LOGGER.info("Bound Voicechat Client API.");
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(MicrophonePacketEvent.class, VoiceAudioRouter::onMicrophonePacket);
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);

        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, ClientAudioProcessor::onSoundReceived);
        registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class,
                ClientAudioProcessor::onSoundReceived);
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, ClientAudioProcessor::onSoundReceived);

        AnalogAudio.LOGGER.info("Registered unified Voicechat Events.");
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        VoicechatServerApi serverApi = event.getVoicechat();
        VoicechatApiHandle.setServerApi(serverApi);
        AnalogAudio.LOGGER.info("Bound Voicechat Server API from Startup Event.");

        VolumeCategory walkie = serverApi.volumeCategoryBuilder()
                .setId("walkie_talkies")
                .setName("Walkie Talkies")
                .setNameTranslationKey("voicechat.category.walkie_talkies")
                .setDescription("Adjust the volume of walkie talkie signals.")
                .setDescriptionTranslationKey("voicechat.category.walkie_talkies.description")
                .build();
        serverApi.registerVolumeCategory(walkie);

        VolumeCategory speaker = serverApi.volumeCategoryBuilder()
                .setId("speaker_blocks")
                .setName("Speaker Blocks")
                .setNameTranslationKey("voicechat.category.speaker_blocks")
                .setDescription("Adjust the volume of audio broadcasted by speakers.")
                .setDescriptionTranslationKey("voicechat.category.speaker_blocks.description")
                .build();
        serverApi.registerVolumeCategory(speaker);

        AnalogAudio.LOGGER.info("AnalogAudio: Registered Voicechat Volume Categories with Safe Icons.");
    }
}
