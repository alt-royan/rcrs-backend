package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@ActivityInterface
public interface S3Activity {

    @ActivityMethod
    String putImage(String key, byte[] imageData, String contentType);

    @ActivityMethod
    void putAudio(String key, File file, Long contentLength, String contentType) throws IOException;

    @ActivityMethod
    File saveUploadedAudioToFile(String uid) throws IOException;
}
