package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.mediaservice.temporal.activity.ValidateAudioActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.ValidatedAudio;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Set;

@Component
@ActivityImpl
@Slf4j
public class ValidateAudioActivityImpl implements ValidateAudioActivity {

    private static final Set<String> ACCEPTED_FORMATS = Set.of("mp3", "wav", "flac", "ogg");
    private static final double MIN_DURATION_SECONDS = 1.0;
    private static final double MAX_DURATION_SECONDS = 7200.0;

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    public ValidateAudioActivityImpl(S3Client s3Client, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public ValidatedAudio validate(String uid) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("audio-validate-", ".tmp");
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder().bucket(s3UploadBucket).key(uid).build());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                s3Stream.transferTo(fos);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "quiet", "-print_format", "json",
                    "-show_format", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ffprobe failed with exit code " + exitCode);
            }

            JsonNode root = objectMapper.readTree(output);
            JsonNode format = root.get("format");

            String container = format.get("name").asText();
            double duration = format.get("duration").asDouble();

            if (!ACCEPTED_FORMATS.contains(container)) {
                throw new BadRequestException("Unsupported audio format: " + container + ". Accepted: mp3, wav, flac, ogg");
            }

            if (duration < MIN_DURATION_SECONDS || duration > MAX_DURATION_SECONDS) {
                throw new BadRequestException("Audio duration " + duration + "s is out of range [1s, 7200s]");
            }

            log.info("Audio validated: uid={}, format={}, duration={}s", uid, container, duration);
            return new ValidatedAudio(uid, container, duration);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to validate audio: " + uid, e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile.toPath()); } catch (Exception ignored) {}
            }
        }
    }
}
