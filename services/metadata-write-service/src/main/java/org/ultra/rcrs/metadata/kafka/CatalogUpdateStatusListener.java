package org.ultra.rcrs.metadata.kafka;

import com.google.protobuf.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.LifecycleStatusOuterClass;
import org.ultra.rcrs.events.track.TrackTranscodingCompletedEventOuterClass;
import org.ultra.rcrs.events.track.TrackUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.service.AlbumService;
import org.ultra.rcrs.metadata.service.TrackService;
import org.ultra.rcrs.utils.Url62;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CatalogUpdateStatusListener {

    private final TrackService trackService;
    private final AlbumService albumService;

    @KafkaListener(topics = Topics.CATALOG_UPDATE_STATUS_TOPIC, groupId = "catalog-write-group", containerFactory = "byteArrayContainerFactory")
    public void handleUpdateStatusEvent(ConsumerRecord<String, byte[]> record) {
        try {
            DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.parseFrom(record.value());
            log.info("Received update status event: type={} aggregate={} id={}", event.getEventType(), event.getAggregateType(), event.getAggregateId());

            switch (event.getEventType()) {
                case TRACK_TRANSCODING_COMPLETED -> onTrackTranscodingCompleted(event.getPayload());
                case TRACK_LIFECYCLE_STATUS_UPDATED -> onTrackLifecycleStatusUpdated(event.getPayload());
                default -> log.debug("Ignored event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process update status event: {}", e.getMessage(), e);
        }
    }

    private void onTrackTranscodingCompleted(Any payload) {
        try {
            TrackTranscodingCompletedEventOuterClass.TrackTranscodingCompletedEvent event =
                    payload.unpack(TrackTranscodingCompletedEventOuterClass.TrackTranscodingCompletedEvent.class);
            UUID trackId = Url62.decode(event.getTrackId());
            LifecycleStatus status = mapLifecycleStatus(event.getStatus());
            trackService.handleTranscodingCompleted(trackId, status, event.getDurationMs());
            var track = trackService.findById(trackId);
            albumService.checkAlbumReady(track.getAlbumId());
        } catch (Exception e) {
            log.error("Failed to unpack TrackTranscodingCompletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackLifecycleStatusUpdated(Any payload) {
        try {
            TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event =
                    payload.unpack(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.class);
            UUID trackId = Url62.decode(event.getId());
            LifecycleStatus status = mapLifecycleStatus(event.getLifecycleStatus());
            trackService.updateLifecycleStatus(status, trackId);
        } catch (Exception e) {
            log.error("Failed to unpack TrackUpdateLifecycleStatusEvent: {}", e.getMessage(), e);
        }
    }

    private LifecycleStatus mapLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus proto) {
        return LifecycleStatus.valueOf(proto.name());
    }
}
