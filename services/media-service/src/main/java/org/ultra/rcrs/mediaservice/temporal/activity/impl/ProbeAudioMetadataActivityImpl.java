package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.mediaservice.config.AudioConfigurationProperties;
import org.ultra.rcrs.mediaservice.temporal.activity.ProbeAudioMetadataActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class ProbeAudioMetadataActivityImpl implements ProbeAudioMetadataActivity {

    private final ObjectMapper objectMapper;
    private final AudioConfigurationProperties properties;

    @Override
    public AudioMetadata probe(File tempFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffprobe",
                    "-v", "quiet",
                    "-print_format", "json",
                    "-show_format",
                    "-show_streams",
                    tempFile.getAbsolutePath());

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

            String container = format.get("format_name").asString();
            long sizeInBytes = format.get("size").asLong();
            double duration = format.get("duration").asDouble();
            int durationMs = (int) (duration * 1000);

            String codec = "";
            String bitrate = "0";
            String sampleRate = "0";
            if (streams != null && !streams.isEmpty()) {
                JsonNode audioStream = streams.get(0);
                codec = audioStream.has("codec_name") ? audioStream.get("codec_name").asString() : "";
                sampleRate = audioStream.has("sample_rate") ? audioStream.get("sample_rate").asString() : "0";
                bitrate = audioStream.has("bit_rate") ? audioStream.get("bit_rate").asString() :
                        (format.has("bit_rate") ? format.get("bit_rate").asString() : "0");
            }

            bitrate = Integer.parseInt(bitrate) / 1000 + "k";

            if (!properties.getValidation().getFormats().contains(container)) {
                throw new BadRequestException("Unsupported audio format: " + container);
            }

            if (Duration.of(durationMs, ChronoUnit.MILLIS).compareTo(properties.getValidation().getDuration().getMin()) < 0
                    || Duration.of(durationMs, ChronoUnit.MILLIS).compareTo(properties.getValidation().getDuration().getMax()) > 0) {
                throw new BadRequestException("Audio duration " +
                        duration + "s is out of range [" + properties.getValidation().getDuration().getMin().toString() +
                        ", " + properties.getValidation().getDuration().getMax().toString() + "]");
            }

            log.info("Probed audio: codec={}, container={}, bitrate={}, sampleRate={}, durationMs={}, size={}",
                    codec, container, bitrate, sampleRate, durationMs, sizeInBytes);
            return new AudioMetadata(codec, container, bitrate, sampleRate, durationMs, sizeInBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to probe audio", e);
        }
    }
}
