package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.config.MediaConfigurationProperties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@Slf4j
@RequiredArgsConstructor
public class ImageStorage {

    private final S3Client s3Client;
    private final MediaConfigurationProperties properties;

    public String putImage(String key, byte[] body, String contentType) {
        String imageBucket = properties.getImage().getBucket().getName();
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(imageBucket)
                        .key(key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromBytes(body));
        log.info("Put image to S3: bucket [{}], key [{}]", imageBucket, key);
        return String.format("s3://%s/%s", imageBucket, key);
    }
}
