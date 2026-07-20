package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.SaveOriginalAudioToS3Activity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@ActivityImpl
@Slf4j
public class SaveOriginalAudioToS3ActivityImpl implements SaveOriginalAudioToS3Activity {

    private final S3Client s3Client;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Value("${cdn.audios.bucket}")
    private String s3AudioBucket;

    public SaveOriginalAudioToS3ActivityImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String save(String trackId, String guid, String uid, String contentType) {
        String s3Key = trackId + "/" + guid;

        ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(s3UploadBucket).key(uid).build());

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(s3AudioBucket)
                        .key(s3Key)
                        .contentType(contentType)
                        .build(),
                RequestBody.fromInputStream(s3Stream, -1));

        log.info("Original audio saved: {}/{}", s3AudioBucket, s3Key);
        return s3Key;
    }
}
