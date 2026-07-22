package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.io.File;

@ActivityInterface
public interface TranscodeAudioActivity {

    @ActivityMethod
    File transcode(File inputFile, String bitrate);
}
