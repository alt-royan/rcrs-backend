package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedImage;

@ActivityInterface
public interface ValidateImageActivity {

    @ActivityMethod
    ValidatedImage validate(String dataUrl);
}
