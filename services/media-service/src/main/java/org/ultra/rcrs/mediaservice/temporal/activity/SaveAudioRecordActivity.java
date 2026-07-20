package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

@ActivityInterface
public interface SaveAudioRecordActivity {

    @ActivityMethod
    void save(String guid, String uploadUid, String trackId, AudioMetadata metadata);
}
