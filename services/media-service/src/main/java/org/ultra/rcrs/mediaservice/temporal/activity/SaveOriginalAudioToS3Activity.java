package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SaveOriginalAudioToS3Activity {

    @ActivityMethod
    String save(String trackId, String guid, String uid, String contentType);
}
