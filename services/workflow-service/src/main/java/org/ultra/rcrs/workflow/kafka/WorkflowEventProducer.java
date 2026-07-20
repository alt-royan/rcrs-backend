package org.ultra.rcrs.workflow.kafka;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.track.TrackTranscodingEventOuterClass;
import org.ultra.rcrs.kafka.ProtobufEventProducer;
import org.ultra.rcrs.kafka.Topics;

import java.time.Instant;
import java.util.UUID;

@Component
public class WorkflowEventProducer extends ProtobufEventProducer {

    @Value("${spring.application.name}")
    private String serviceName;

    public WorkflowEventProducer(@Autowired KafkaTemplate<String, byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void trackTranscoding(String uid, String trackId) {
        var event = TrackTranscodingEventOuterClass.TrackTranscodingEvent.newBuilder()
                .setUid(uid)
                .setTrackId(trackId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_TRANSCODING)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(trackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEventAsync(domainEvent, Topics.MEDIA_TRANSCODING_TOPIC);
    }
}
