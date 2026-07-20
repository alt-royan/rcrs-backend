package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.temporal.activity.ProbeAudioMetadataActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;

@Component
@ActivityImpl
@Slf4j
public class ProbeAudioMetadataActivityImpl implements ProbeAudioMetadataActivity {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "mp3", "audio/mpeg",
            "wav", "audio/wav",
            "flac", "audio/flac",
            "ogg", "audio/ogg"
    );

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Value("${cdn.audios.bucket}")
    private String s3AudioBucket;

    public ProbeAudioMetadataActivityImpl(S3Client s3Client, ObjectMapper objectMapper) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
    }

    @Override
    public AudioMetadata probe(String key, boolean isOriginal) {
        String bucket = isOriginal ? s3UploadBucket : s3AudioBucket;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("audio-probe-", ".tmp");
            ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(key).build());
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                s3Stream.transferTo(fos);
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe", "-v", "quiet", "-print_format", "json",
                    "-show_format", "-show_streams", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("ffprobe failed with exit code " + exitCode);
            }

            JsonNode root = objectMapper.readTree(output);
            JsonNode format = root.get("format");
            JsonNode streams = root.get("streams");

            String container = format.get("name").asText();
            long sizeInBytes = format.get("size").asLong();
            double duration = format.get("duration").asDouble();
            long durationMs = (long) (duration * 1000);

            String codec = "";
            String bitrate = "0";
            String sampleRate = "0";
            if (streams != null && !streams.isEmpty()) {
                JsonNode audioStream = streams.get(0);
                codec = audioStream.has("codec_name") ? audioStream.get("codec_name").asText() : "";
                sampleRate = audioStream.has("sample_rate") ? audioStream.get("sample_rate").asText() : "0";
                bitrate = audioStream.has("bit_rate") ? audioStream.get("bit_rate").asText() :
                        (format.has("bit_rate") ? format.get("bit_rate").asText() : "0");
            }

            String contentType = CONTENT_TYPES.getOrDefault(container, "application/octet-stream");

            log.info("Probed audio: key={}, codec={}, container={}, bitrate={}, sampleRate={}, durationMs={}, size={}",
                    key, codec, container, bitrate, sampleRate, durationMs, sizeInBytes);
            return new AudioMetadata(codec, container, bitrate, sampleRate, durationMs, sizeInBytes, contentType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to probe audio: " + key, e);
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile.toPath()); } catch (Exception ignored) {}
            }
        }
    }
}
