package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.AudioActivity;
import org.ultra.rcrs.workflowservice.client.AudioClient;

@Component
public class AudioActivityImpl implements AudioActivity {

    private final AudioClient audioClient;

    public AudioActivityImpl(AudioClient audioClient) {
        this.audioClient = audioClient;
    }

    @Override
    public String uploadAudio(String trackId, String audioFileUrl) {
        return audioClient.uploadAudio(trackId, audioFileUrl);
    }

    @Override
    public String getAudioStatus(String trackId) {
        return audioClient.getAudioStatus(trackId);
    }

    @Override
    public void deleteAudio(String trackId) {
        audioClient.deleteAudio(trackId);
    }
}
