package org.ultra.rcrs.mediaservice.temporal.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dto.TranscodingWorkflowInput;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = MEDIA_TASK_QUEUE)
public class AudioTranscodingWorkflowImpl implements AudioTranscodingWorkflow {

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

            AudioMetadata originalMeta = activities.probeAudioMetadataActivity().probe(tempFile);
            String key = String.format("%s/%s/%s", trackId, guid, audioUpload.getOriginalFileName());

            activities.s3Activity().putAudio(key, tempFile, originalMeta.byteSize(), audioUpload.getContentType());

            activities.dbActivity().saveAudio(trackId, guid, true, key, originalMeta);

            for (String bitrate : bitrates) {
                File outputFile = activities.transcodeAudioActivity().transcode(tempFile, bitrate);

                AudioMetadata metadata = activities.probeAudioMetadataActivity().probe(outputFile);
                key = String.format("%s/%s/%s_%s", trackId, guid, metadata.container(), bitrate);

                activities.s3Activity().putAudio(key, outputFile, metadata.byteSize(), "audio/ogg");

                activities.dbActivity().saveAudio(trackId, guid, true, key, metadata);

                if (outputFile != null) {
                    try {
                        Files.deleteIfExists(outputFile.toPath());
                    } catch (Exception ignored) {
                    }
                }
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
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (Exception ignored) {
                }
            }
        }
    }
}
