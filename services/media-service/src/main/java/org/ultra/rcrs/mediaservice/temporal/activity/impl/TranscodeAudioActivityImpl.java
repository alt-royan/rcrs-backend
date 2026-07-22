package org.ultra.rcrs.mediaservice.temporal.activity.impl;

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
            File outputFile = File.createTempFile("audio-transcode-output-", ".ogg");

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
            throw new RuntimeException("Failed to transcode audio bitrate=" + bitrate, e);
        }
    }

    private @NonNull Process getProcess(File inputFile, String bitrate, File outputFile) throws IOException {
        String loudnorm = String.format("loudnorm=I=%s:LRA=%s:TP=%s", properties.getLoudnorm().getI(), properties.getLoudnorm().getLRA(), properties.getLoudnorm().getTP());
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-i", inputFile.getAbsolutePath(),
                "-af", loudnorm,
                "-c:a", properties.getCodec(),
                "-b:a", bitrate,
                "-ar", properties.getRate(),
                "-vn",
                "-map_metadata", "-1",
                "-y",
                "-f", properties.getFormat(),
                outputFile.getAbsolutePath());
        pb.redirectErrorStream(false);
        return pb.start();
    }
}
