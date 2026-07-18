package org.ultra.rcrs.workflowservice.client;

public interface AudioClient {

    String uploadAudio(String trackId, String audioFileUrl);

    String getAudioStatus(String trackId);

    void deleteAudio(String trackId);
}
