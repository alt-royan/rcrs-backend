package org.ultra.rcrs.mediaservice.service.transcoding;

import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.pipeline.Handler;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.OutputStream;

@Slf4j
public class S3ProcessHandler implements Handler<AudioProcessData, AudioProcessData> {

    private final S3Client s3Client;

    public S3ProcessHandler(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public AudioProcessData process(AudioProcessData input) {
        String key = String.format("%s/%s_%s/%s", input.getTrackId(), input.getContainer(), input.getBitrate(), input.getGuid());
        try {
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder().bucket(input.getInputBucket()).key(input.getInputKey()).build());

            Process process = input.getProcessBuilder().start();
            OutputStream processStdIn = process.getOutputStream();

            s3Stream.transferTo(processStdIn);
            processStdIn.flush();

            process.waitFor();
            var bytes = process.getInputStream().readAllBytes();
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(input.getOutputBucket())
                    .key(key)
                    .contentType(input.getContentType())
                    .build(), RequestBody.fromBytes(bytes));
            log.info("Successfully upload audio with key {}", key);
            return input;
        } catch (Exception e) {
            log.error("Exception while uploading audio with key {}", key);
            throw new RuntimeException(e);
        }
    }
}
