package org.ultra.rcrs.mediaservice.temporal.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dto.TranscodingWorkflowInput;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = MEDIA_TASK_QUEUE)
public class AudioTranscodingWorkflowImpl implements AudioTranscodingWorkflow {

    private static final String AUDIO_CONTENT_TYPE = "audio/ogg";

    private final List<String> bitrates;

    public AudioTranscodingWorkflowImpl(List<String> bitrates) {
        this.bitrates = bitrates == null ? new ArrayList<>() : bitrates;
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

            AudioMetadata originalMeta = activities.probeAudioMetadataActivity().probe(tempFile);
            String key = String.format("%s/%s/%s", trackId, guid, originalFilename);

            activities.s3Activity().putAudio(key, tempFile, originalMeta.byteSize(), audioUpload.getContentType());

            activities.dbActivity().saveAudio(trackId, guid, true, key, originalMeta);

            File bestFile = null;
            AudioMetadata bestMetadata = null;
            UUID bestAudioId = null;

            for (String bitrate : bitrates) {
                File outputFile = activities.transcodeAudioActivity().transcode(tempFile, bitrate);

                AudioMetadata metadata = activities.probeAudioMetadataActivity().probe(outputFile);
                key = String.format("%s/%s/%s_%s", trackId, guid, metadata.container(), bitrate);

                activities.s3Activity().putAudio(key, outputFile, metadata.byteSize(), AUDIO_CONTENT_TYPE);

                UUID audioId = activities.dbActivity().saveAudio(trackId, guid, true, key, metadata);

                // keep the highest quality rendition around, it becomes the downloadable file
                if (bestMetadata == null || metadata.byteSize() > bestMetadata.byteSize()) {
                    deleteQuietly(bestFile);
                    bestFile = outputFile;
                    bestMetadata = metadata;
                    bestAudioId = audioId;
                } else {
                    deleteQuietly(outputFile);
                }
            }

            //TODO: переделать даунлоад для всех качеств
            if (bestFile != null) {
                String downloadFileName = downloadFileName(audioUpload.getOriginalFileName(), bestMetadata.container());
                String downloadKey = String.format("%s/%s/%s", trackId, guid,
                        URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8));

                activities.s3Activity().putDownload(downloadKey, bestFile, bestMetadata.byteSize(),
                        AUDIO_CONTENT_TYPE, downloadFileName);
                activities.dbActivity().saveDownloadFile(bestAudioId, guid, downloadKey, downloadFileName, AUDIO_CONTENT_TYPE);

                deleteQuietly(bestFile);
            }

            activities.transcodingStatusActivity().updateStatusToComplete(uid);
            activities.transcodingStatusActivity().notifyTranscodingSuccess(trackId, originalMeta.durationMs());
            log.info("Audio transcoding workflow completed for uid={}", uid);

        } catch (Exception e) {
            log.error("Audio transcoding workflow failed for uid={}: {}", uid, e.getMessage());
            activities.transcodingStatusActivity().updateStatusToFailed(uid, e.getMessage());
            activities.transcodingStatusActivity().notifyTranscodingFailed(trackId);
            throw new RuntimeException("Audio transcoding failed for uid=" + uid, e);
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
