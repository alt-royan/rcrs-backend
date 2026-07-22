package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.io.File;

@ActivityInterface
public interface ProbeAudioMetadataActivity {

    @ActivityMethod
    AudioMetadata probe(File tempFile);
}
