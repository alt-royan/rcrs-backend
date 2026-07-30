package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.config.AudioConfigurationProperties;
import org.ultra.rcrs.mediaservice.temporal.activity.TranscodeAudioActivity;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class TranscodeAudioActivityImpl implements TranscodeAudioActivity {

    private final AudioConfigurationProperties properties;

    @Override
    public File transcode(File inputFile, String bitrate) {
        try {
            File outputFile = File.createTempFile("audio-transcode-output-", "." + properties.getFormat());

            Process process = getProcess(inputFile, bitrate, outputFile);

            boolean finished = process.waitFor(5, TimeUnit.MINUTES);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("FFmpeg timed out for bitrate=" + bitrate);
            }
            if (process.exitValue() != 0) {
                String stderr = new String(process.getErrorStream().readAllBytes());
                throw new RuntimeException("FFmpeg failed (exit=" + process.exitValue() + "): " + stderr);
            }

            log.info("Audio transcoding completed: (bitrate={})", bitrate);
            return outputFile;
        } catch (Exception e) {
            throw ApplicationFailure.newNonRetryableFailure("Failed to transcode audio bitrate=" + bitrate + ": " + e.getMessage(), e.getClass().getName(), e);
        }
    }

    private @NonNull Process getProcess(File inputFile, String bitrate, File outputFile) throws IOException {
        var enabled = properties.getLoudnorm().getEnabled();
        ProcessBuilder pb;
        if (enabled) {
            String loudnorm = String.format("loudnorm=I=%s:LRA=%s:TP=%s", properties.getLoudnorm().getI(), properties.getLoudnorm().getLRA(), properties.getLoudnorm().getTP());
            pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.getAbsolutePath(),
                    "-af", loudnorm,
                    "-c:a", properties.getCodec(),
                    "-b:a", bitrate,
                    "-ar", properties.getRate(),
                    "-movflags", "+faststart",
                    "-vn",
                    "-map_metadata", "-1",
                    "-y",
                    "-f", properties.getFormat(),
                    outputFile.getAbsolutePath());
        } else {
            pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputFile.getAbsolutePath(),
                    "-c:a", properties.getCodec(),
                    "-b:a", bitrate,
                    "-ar", properties.getRate(),
                    "-movflags", "+faststart",
                    "-vn",
                    "-map_metadata", "-1",
                    "-y",
                    "-f", properties.getFormat(),
                    outputFile.getAbsolutePath());
        }
        pb.redirectError(ProcessBuilder.Redirect.PIPE);
        return pb.start();
    }
}
