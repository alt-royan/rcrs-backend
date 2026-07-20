package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.NormalizeAudioActivity;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Component
@ActivityImpl
@Slf4j
public class NormalizeAudioActivityImpl implements NormalizeAudioActivity {

    private final S3Client s3Client;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Value("${cdn.audios.bucket}")
    private String s3AudioBucket;

    public NormalizeAudioActivityImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String normalize(String uid, String trackId, String guid, String bitrate) {
        File inputFile = null;
        File outputFile = null;
        try {
            inputFile = File.createTempFile("audio-normalize-input-", ".tmp");
            outputFile = File.createTempFile("audio-normalize-output-", ".ogg");

            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder().bucket(s3UploadBucket).key(uid).build());
            try (FileOutputStream fos = new FileOutputStream(inputFile)) {
                s3Stream.transferTo(fos);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.getAbsolutePath(),
                    "-af", "loudnorm",
                    "-c:a", "libvorbis",
                    "-b:a", String.valueOf(bitrate),
                    "-ar", "44100",
                    "-vn",
                    "-map_metadata", "-1",
                    "-y",
                    "-f", "ogg",
                    outputFile.getAbsolutePath());
            pb.redirectErrorStream(false);
            Process process = pb.start();

            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg timed out for uid=" + uid + " bitrate=" + bitrate);
            }
            if (process.exitValue() != 0) {
                String stderr = new String(process.getErrorStream().readAllBytes());
                throw new RuntimeException("FFmpeg failed (exit=" + process.exitValue() + "): " + stderr);
            }

            String s3Key = trackId + "/" + guid + "/ogg_" + bitrate;

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(s3AudioBucket)
                            .key(s3Key)
                            .contentType("audio/ogg")
                            .build(),
                    RequestBody.fromFile(outputFile));

            log.info("Normalized audio saved: {}/{} (bitrate={})", s3AudioBucket, s3Key, bitrate);
            return s3Key;
        } catch (Exception e) {
            throw new RuntimeException("Failed to normalize audio uid=" + uid + " bitrate=" + bitrate, e);
        } finally {
            if (inputFile != null) { try { Files.deleteIfExists(inputFile.toPath()); } catch (Exception ignored) {} }
            if (outputFile != null) { try { Files.deleteIfExists(outputFile.toPath()); } catch (Exception ignored) {} }
        }
    }
}
