package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.client.AudioClient;

@Component
public class AudioClientImpl implements AudioClient {

    @Override
    public String uploadAudio(String trackId, String audioFileUrl) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getAudioStatus(String trackId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void deleteAudio(String trackId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
