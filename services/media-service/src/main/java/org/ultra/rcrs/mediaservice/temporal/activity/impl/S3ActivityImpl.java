package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.config.MediaConfigurationProperties;
import org.ultra.rcrs.mediaservice.temporal.activity.S3Activity;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class S3ActivityImpl implements S3Activity {

    private final S3Client s3Client;
    private final MediaConfigurationProperties properties;

    @Override
    public void putAudio(String key, File file, Long contentLength, String contentType) throws IOException {
        String audioBucket = properties.getAudio().getBucket().getName();
        try (InputStream is = new FileInputStream(file)) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(audioBucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromInputStream(is, contentLength));
        }

        log.info("Put audio to S3: bucket [{}], key [{}]", audioBucket, key);
    }

    @Override
    public void putAudio(String key, File file, Long contentLength) throws IOException {
        putAudio(key, file, contentLength, properties.getAudio().getContentType());
    }

    @Override
    public void putDownload(String key, File file, Long contentLength, String contentType, String fileName) throws IOException {
        String downloadBucket = properties.getDownload().getBucket().getName();
        String contentDisposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build().toString();
        try (InputStream is = new FileInputStream(file)) {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(downloadBucket)
                            .key(key)
                            .contentType(contentType)
                            .contentDisposition(contentDisposition)
                            .build(),
                    RequestBody.fromInputStream(is, contentLength));
        }

        log.info("Put download file to S3: bucket [{}], key [{}]", downloadBucket, key);
    }

    @Override
    public void putDownload(String key, File file, Long contentLength, String fileName) throws IOException {
        putDownload(key, file, contentLength, properties.getAudio().getContentType(), fileName);
    }

    @Override
    public File saveUploadedAudioToFile(String uid) throws IOException {
        String uploadBucket = properties.getUpload().getBucket().getName();
        File tmp = File.createTempFile("audio-probe-", ".tmp");
        try (var s3Stream = s3Client.getObject(GetObjectRequest.builder().bucket(uploadBucket).key(uid).build())) {
            Files.copy(s3Stream, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return tmp;
        } catch (IOException e) {
            log.error("Failed when try to save s3 audio to tmp", e);
            throw new RuntimeException(e);
        }
    }
}
