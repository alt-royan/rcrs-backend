package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.util.UUID;

@ActivityInterface
public interface DbActivity {

    @ActivityMethod
    UUID saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata);

    @ActivityMethod
    void saveDownloadFile(UUID audioId, UUID guid, String key, String fileName, String contentType);

    @ActivityMethod
    AudioUpload getAudioUpload(String uid);
}
