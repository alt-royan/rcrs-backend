package org.ultra.rcrs.mediaservice.producer;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.LifecycleStatusOuterClass;
import org.ultra.rcrs.events.track.TrackUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.kafka.ProtobufEventProducer;
import org.ultra.rcrs.kafka.Topics;

import java.time.Instant;
import java.util.UUID;

@Component
public class MediaEventProducer extends ProtobufEventProducer {

    @Value("${spring.application.name}")
    private String serviceName;

    public MediaEventProducer(@Autowired KafkaTemplate<String, byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void failedTranscoding(String trackId) {
        sendLifecycleStatus(trackId, LifecycleStatusOuterClass.LifecycleStatus.FAILED);
    }

    public void startTranscoding(String trackId) {
        sendLifecycleStatus(trackId, LifecycleStatusOuterClass.LifecycleStatus.TRANSCODING);
    }

    public void successTranscoding(String trackId) {
        sendLifecycleStatus(trackId, LifecycleStatusOuterClass.LifecycleStatus.READY);
    }

    private void sendLifecycleStatus(String trackId, LifecycleStatusOuterClass.LifecycleStatus status) {
        var event = TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.newBuilder()
                .setId(trackId)
                .setLifecycleStatus(status)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(trackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_UPDATE_STATUS_TOPIC);
    }
}
