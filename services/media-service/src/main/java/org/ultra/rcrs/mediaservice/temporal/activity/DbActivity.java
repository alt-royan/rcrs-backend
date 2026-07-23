package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.util.UUID;

@ActivityInterface
public interface DbActivity {

    @ActivityMethod
    void saveAudio(String trackId, UUID guid, String key, AudioMetadata metadata);

    @ActivityMethod
    AudioUpload getAudioUpload(String uid);
}
