package org.ultra.rcrs.workflowservice.activity;

public interface AudioActivity {

    String uploadAudio(String trackId, String audioFileUrl);

    String getAudioStatus(String trackId);

    void deleteAudio(String trackId);
}
