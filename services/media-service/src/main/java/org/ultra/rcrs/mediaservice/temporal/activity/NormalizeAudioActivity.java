package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface NormalizeAudioActivity {

    @ActivityMethod
    String normalize(String uid, String trackId, String guid, String bitrate);
}
