package org.ultra.rcrs.mediaservice.temporal.workflow.impl;

import io.temporal.failure.ApplicationFailure;
import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.mediaservice.config.AudioConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dto.TranscodingWorkflowInput;
import org.ultra.rcrs.mediaservice.enums.Quality;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = MEDIA_TASK_QUEUE)
public class AudioTranscodingWorkflowImpl implements AudioTranscodingWorkflow {

    private final Map<Quality, String> qualityMap = new HashMap<>();

    public AudioTranscodingWorkflowImpl(AudioConfigurationProperties.Quality quality) {
        qualityMap.put(Quality.LOW, quality.getLow());
        qualityMap.put(Quality.MID, quality.getMid());
        qualityMap.put(Quality.HIGH, quality.getHigh());
    }

    @Override
    public void transcode(TranscodingWorkflowInput input) {
        String uid = input.getUid();
        String trackId = input.getTrackId();
        log.info("Starting audio transcoding workflow for uid={}, trackId={}", uid, trackId);
        ActivityFactory activities = ActivityFactory.getInstance();
        AudioUpload audioUpload = activities.dbActivity().getAudioUpload(uid);
        File tempFile = null;
        try {
            activities.transcodingStatusActivity().updateStatusToTranscoding(uid, trackId);
            activities.transcodingStatusActivity().notifyTranscodingStarted(trackId);

            tempFile = activities.s3Activity().saveUploadedAudioToFile(uid);

            UUID guid = UUID.randomUUID();
            String originalFilename = URLEncoder.encode(audioUpload.getOriginalFileName(), StandardCharsets.UTF_8);

            AudioMetadata originalMeta = activities.probeAudioMetadataActivity().probe(tempFile, true);
            String key = String.format("%s/%s/%s", trackId, guid, originalFilename);

            activities.s3Activity().putAudioWithContentType(key, tempFile, originalMeta.byteSize(), audioUpload.getContentType());
            String downloadFileName = downloadFileName(audioUpload.getOriginalFileName(), originalMeta.container());
            activities.s3Activity().putDownloadWithContentType(key, tempFile, originalMeta.byteSize(), audioUpload.getContentType(), downloadFileName);

            activities.dbActivity().saveAudioWithContentType(trackId, guid, true, key, originalMeta, Quality.ORIGINAL, audioUpload.getContentType());

            for (Map.Entry<Quality, String> q : qualityMap.entrySet()) {
                File outputFile = activities.transcodeAudioActivity().transcode(tempFile, q.getValue());

                AudioMetadata metadata = activities.probeAudioMetadataActivity().probe(outputFile, false);
                key = String.format("%s/%s/%s_%s", trackId, guid, metadata.container(), q.getValue());

                activities.s3Activity().putAudio(key, outputFile, metadata.byteSize());
                downloadFileName = downloadFileName(audioUpload.getOriginalFileName(), metadata.container());
                activities.s3Activity().putDownload(key, outputFile, metadata.byteSize(), downloadFileName);

                activities.dbActivity().saveAudio(trackId, guid, true, key, metadata, q.getKey());

                deleteQuietly(outputFile);
            }

            activities.transcodingStatusActivity().updateStatusToComplete(uid);
            activities.transcodingStatusActivity().notifyTranscodingSuccess(trackId, originalMeta.durationMs());
            log.info("Audio transcoding workflow completed for uid={}", uid);

        } catch (Exception e) {
            log.error("Audio transcoding workflow failed for uid={}: {}", uid, e.getMessage());
            activities.transcodingStatusActivity().updateStatusToFailed(uid, e.getMessage());
            activities.transcodingStatusActivity().notifyTranscodingFailed(trackId);
            throw ApplicationFailure.newNonRetryableFailure("Audio transcoding failed for uid=" + uid, e.getClass().getName(), e);
        } finally {
            deleteQuietly(tempFile);
        }
    }

    private static String downloadFileName(String originalFileName, String container) {
        String baseName = originalFileName.contains(".")
                ? originalFileName.substring(0, originalFileName.lastIndexOf('.'))
                : originalFileName;
        String extension = container.contains(",") ? container.substring(0, container.indexOf(',')) : container;
        return baseName + "." + extension;
    }

    private static void deleteQuietly(File file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception ignored) {
        }
    }
}
