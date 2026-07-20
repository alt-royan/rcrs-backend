package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface TranscodingStatusActivity {

    @ActivityMethod
    void updateStatusToTranscoding(String uid, String trackId);

    @ActivityMethod
    void updateStatusToFailed(String uid, String error);

    @ActivityMethod
    void updateStatusToComplete(String uid);

    @ActivityMethod
    void notifyTranscodingStarted(String trackId);

    @ActivityMethod
    void notifyTranscodingFailed(String trackId);

    @ActivityMethod
    void notifyTranscodingSuccess(String trackId);
}
