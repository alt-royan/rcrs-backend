package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface SaveThumbnailActivity {

    @ActivityMethod
    void saveThumbnail(String key, byte[] imageData, String format, String contentType, int size);
}
