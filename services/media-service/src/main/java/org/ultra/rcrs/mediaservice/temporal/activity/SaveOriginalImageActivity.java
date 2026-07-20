package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SaveOriginalImageActivity {

    @ActivityMethod
    String saveOriginal(String key, byte[] imageData, String contentType);
}
