package org.ultra.rcrs.catalogservice.kafka;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.Artist;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistDeletedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistHiddenEventOuterClass;
import org.ultra.rcrs.events.common.AvailabilityStatusOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.SocialLinkOuterClass;
import org.ultra.rcrs.kafka.ProtobufEventProducer;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.UUID;

@Component
public class CatalogEventProducer extends ProtobufEventProducer {

    @Value("${apring.application.name}")
    private String serviceName;

    public CatalogEventProducer(@Autowired KafkaTemplate<String, byte[]> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void artistCreated(Artist artist) {
        String artistId = Url62.encode(artist.getId());
        var event = ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                .setId(artistId)
                .setName(artist.getName())
                .setAvatarS3Key(artist.getAvatarS3Key())
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(artist.getAvailabilityStatus().name()))
                .addAllTags(artist.getTags())
                .addAllSocialLinks(artist.getSocialLinks().getItems().stream().map(link -> SocialLinkOuterClass.SocialLink.newBuilder()
                        .setResourceName(link.getResourceName())
                        .setUrl(link.getUrl()).build()).toList())
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_CREATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(artistId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistDeleted(UUID artistId) {
        String stringId = Url62.encode(artistId);
        var event = ArtistDeletedEventOuterClass.ArtistDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistHidden(UUID artistId) {
        String stringId = Url62.encode(artistId);
        var event = ArtistHiddenEventOuterClass.ArtistHiddenEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void albumDeleted(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = ArtistDeletedEventOuterClass.ArtistDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void albumHidden(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = ArtistHiddenEventOuterClass.ArtistHiddenEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void trackDeleted(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = ArtistDeletedEventOuterClass.ArtistDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void trackHidden(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = ArtistHiddenEventOuterClass.ArtistHiddenEvent.newBuilder()
                .setId(stringId)
                .build();

        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setNanos(Instant.now().getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

}
