package org.ultra.rcrs.mediaservice.listener;

import com.google.protobuf.Any;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.track.TrackTranscodingEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.mediaservice.temporal.workflow.AudioTranscodingWorkflow;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.ultra.rcrs.mediaservice.temporal.config.TemporalConfig.MEDIA_TASK_QUEUE;

@Component
@RequiredArgsConstructor
@Slf4j
public class MediaEventListener {

    private final WorkflowClient workflowClient;

    @KafkaListener(topics = Topics.MEDIA_TRANSCODING_TOPIC, groupId = "media-group", containerFactory = "byteArrayContainerFactory")
    public void handleTrackEvent(ConsumerRecord<String, byte[]> record) {
        try {
            DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.parseFrom(record.value());
            log.info("Received CDC event: type={} aggregate={} id={}", event.getEventType(), event.getAggregateType(), event.getAggregateId());

            switch (event.getEventType()) {
                case TRACK_TRANSCODING -> onTrackTranscoding(event.getPayload());
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process CDC event: {}", e.getMessage(), e);
        }
    }

    private void onTrackTranscoding(Any payload) {
        try {
            TrackTranscodingEventOuterClass.TrackTranscodingEvent event = payload.unpack(TrackTranscodingEventOuterClass.TrackTranscodingEvent.class);
            if (!StringUtils.isEmpty(event.getTrackId()) && !StringUtils.isEmpty(event.getUid())) {
                AudioTranscodingWorkflow workflow = workflowClient.newWorkflowStub(
                        AudioTranscodingWorkflow.class,
                        WorkflowOptions.newBuilder()
                                .setTaskQueue(MEDIA_TASK_QUEUE)
                                .setWorkflowId(UUID.randomUUID().toString())
                                .build()
                );
                WorkflowClient.execute(workflow::transcode, event.getUid(), event.getTrackId());
            }
        } catch (Exception e) {
            log.error("Failed to unpack ArtistCreatedEvent: {}", e.getMessage(), e);
        }
    }
}
