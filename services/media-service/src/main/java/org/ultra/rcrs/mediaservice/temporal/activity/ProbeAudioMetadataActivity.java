package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

@ActivityInterface
public interface ProbeAudioMetadataActivity {

    @ActivityMethod
    AudioMetadata probe(String key, boolean isOriginal);
}
