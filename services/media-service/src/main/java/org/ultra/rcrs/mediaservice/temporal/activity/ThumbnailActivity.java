package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ThumbnailActivity {

    @ActivityMethod
    byte[] createThumbnail(byte[] imageData, String format, int size);
}
