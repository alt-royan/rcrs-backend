package org.ultra.rcrs.mediaservice.temporal.workflow.impl;

import io.temporal.spring.boot.WorkflowImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.ultra.rcrs.mediaservice.temporal.activity.ActivityFactory;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;

import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Slf4j
@WorkflowImpl(taskQueues = MEDIA_TASK_QUEUE)
public class AudioTranscodingWorkflowImpl implements AudioTranscodingWorkflow {

    private final String[] bitrates;

    public AudioTranscodingWorkflowImpl(String[] bitrates) {
        this.bitrates = bitrates;
    }

    @Override
    public void transcode(String uid, String trackId) {
        log.info("Starting audio transcoding workflow for uid={}, trackId={}", uid, trackId);
        ActivityFactory activities = ActivityFactory.getInstance();
        AudioMetadata originalMeta;
        try {
            activities.transcodingStatusActivity().updateStatusToTranscoding(uid, trackId);
            activities.transcodingStatusActivity().notifyTranscodingStarted(trackId);

            activities.validateAudioActivity().validate(uid);
            originalMeta = activities.probeAudioMetadataActivity().probe(uid, true);

            String guid = UUID.randomUUID().toString();

            activities.saveOriginalAudioToS3Activity().save(trackId, guid, uid, originalMeta.contentType());
            activities.saveAudioRecordActivity().save(guid, uid, trackId, originalMeta);

            for (String bitrate : bitrates) {
                String convertedKey = activities.normalizeAudioActivity().normalize(uid, trackId, guid, bitrate);
                AudioMetadata convertedMeta = activities.probeAudioMetadataActivity().probe(convertedKey, false);
                activities.saveAudioRecordActivity().save(guid, uid, trackId, convertedMeta);
            }

        } catch (Exception e) {
            log.error("Audio transcoding workflow failed for uid={}: {}", uid, e.getMessage());
            activities.transcodingStatusActivity().updateStatusToFailed(uid, e.getMessage());
            activities.transcodingStatusActivity().notifyTranscodingFailed(trackId);
            throw new RuntimeException("Audio transcoding failed for uid=" + uid, e);
        }

        activities.transcodingStatusActivity().updateStatusToComplete(uid);
        activities.transcodingStatusActivity().notifyTranscodingSuccess(trackId, originalMeta.durationMs());
        log.info("Audio transcoding workflow completed for uid={}", uid);
    }
}
