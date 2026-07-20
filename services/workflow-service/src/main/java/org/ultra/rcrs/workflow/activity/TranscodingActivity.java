package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface TranscodingActivity {

    @ActivityMethod
    void trackTranscoding(String uid, String trackId);
}
