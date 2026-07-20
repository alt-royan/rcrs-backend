package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.SaveOriginalImageActivity;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ActivityImpl
@Slf4j
public class SaveOriginalImageActivityImpl implements SaveOriginalImageActivity {

    private final S3Client s3Client;

    @Value("${cdn.images.bucket}")
    private String s3ImageBucket;

    public SaveOriginalImageActivityImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String saveOriginal(String key, byte[] imageData, String contentType) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(s3ImageBucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(imageData));

        String uri = String.format("s3://%s/%s", s3ImageBucket, key);
        log.info("Original image saved: {}", uri);
        return uri;
    }
}
