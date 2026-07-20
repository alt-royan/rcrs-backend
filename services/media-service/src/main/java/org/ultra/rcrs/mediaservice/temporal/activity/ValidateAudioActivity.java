package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedAudio;

@ActivityInterface
public interface ValidateAudioActivity {

    @ActivityMethod
    ValidatedAudio validate(String uid);
}
