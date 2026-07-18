package org.ultra.rcrs.catalogservice.kafka;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.model.Artist;
import org.ultra.rcrs.catalogservice.model.OtherArtist;
import org.ultra.rcrs.catalogservice.model.Track;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistDeletedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistHiddenEventOuterClass;
import org.ultra.rcrs.events.common.AlbumTypeOuterClass;
import org.ultra.rcrs.events.common.ArtistRoleOuterClass;
import org.ultra.rcrs.events.common.AvailabilityStatusOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.LifecycleStatusOuterClass;
import org.ultra.rcrs.events.common.SocialLinkOuterClass;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.ArtistDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.TrackCreatedEventOuterClass;
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
                .setAvatarS3Key(artist.getAvatarS3Key() != null ? artist.getAvatarS3Key() : "")
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(artist.getAvailabilityStatus().name()))
                .addAllTags(artist.getTags())
                .addAllSocialLinks(artist.getSocialLinks().getItems().stream().map(link -> SocialLinkOuterClass.SocialLink.newBuilder()
                        .setResourceName(link.getResourceName())
                        .setUrl(link.getUrl()).build()).toList())
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_CREATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(artistId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void albumCreated(Album album) {
        String albumId = Url62.encode(album.getId());

        var eventBuilder = AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                .setId(albumId)
                .setTitle(album.getTitle())
                .setType(AlbumTypeOuterClass.AlbumType.valueOf(album.getType().name()))
                .setCoverS3Key(album.getCoverS3Key() != null ? album.getCoverS3Key() : "")
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(album.getAvailabilityStatus().name()))
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(album.getLifecycleStatus().name()));

        if (album.getReleaseDate() != null) {
            Instant instant = album.getReleaseDate().toInstant();
            eventBuilder.setReleaseDate(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano()));
        }

        var event = eventBuilder.build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_CREATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(albumId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void trackCreated(Track track) {
        String trackId = Url62.encode(track.getId());
        String albumId = Url62.encode(track.getAlbumId());

        var eventBuilder = TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                .setId(trackId)
                .setTitle(track.getTitle())
                .setDurationMs(track.getDurationMs() != null ? track.getDurationMs() : 0)
                .setTrackNumber(track.getTrackNumber())
                .setExplicit(track.getExplicit())
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(track.getAvailabilityStatus().name()))
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(track.getLifecycleStatus().name()))
                .setAlbumId(albumId);

        if (track.getReleaseDate() != null) {
            Instant instant = track.getReleaseDate().toInstant();
            eventBuilder.setReleaseDate(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano()));
        }

        var event = eventBuilder.build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_CREATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(trackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
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

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_HIDDEN)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistAddedToTrack(UUID artistId, UUID trackId, ArtistRole role) {
        String stringArtistId = Url62.encode(artistId);
        String stringTrackId = Url62.encode(trackId);
        var event = ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                .setArtistId(stringArtistId)
                .setTrackId(stringTrackId)
                .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringTrackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistAddedToAlbum(UUID artistId, UUID albumId, ArtistRole role) {
        String stringArtistId = Url62.encode(artistId);
        String stringAlbumId = Url62.encode(albumId);
        var event = ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                .setArtistId(stringArtistId)
                .setAlbumId(stringAlbumId)
                .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringAlbumId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void otherAddedToTrack(OtherArtist other, UUID trackUuid) {
        String stringOtherId = Url62.encode(other.getId());
        String stringTrackId = Url62.encode(trackUuid);
        var event = OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.newBuilder()
                .setOtherId(stringOtherId)
                .setTrackId(stringTrackId)
                .setName(other.getName())
                .addAllRoles(other.getRoles().stream()
                        .map(r -> ArtistRoleOuterClass.ArtistRole.valueOf(r.name())).toList())
                .addAllSocialLinks(other.getSocialLinks().getItems().stream()
                        .map(link -> SocialLinkOuterClass.SocialLink.newBuilder()
                                .setResourceName(link.getResourceName())
                                .setUrl(link.getUrl()).build()).toList())
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.OTHER_ADDED_TO_TRACK)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringTrackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void otherDeletedFromTrack(UUID otherId, UUID trackId) {
        String stringOtherId = Url62.encode(otherId);
        String stringTrackId = Url62.encode(trackId);
        var event = OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent.newBuilder()
                .setOtherId(stringOtherId)
                .setTrackId(stringTrackId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.OTHER_DELETED_FROM_TRACK)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringTrackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistDeletedFromAlbum(UUID artistUuid, UUID albumUuid) {
        String stringArtistId = Url62.encode(artistUuid);
        String stringAlbumId = Url62.encode(albumUuid);
        var event = ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.newBuilder()
                .setArtistId(stringArtistId)
                .setAlbumId(stringAlbumId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringAlbumId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }

    public void artistDeletedFromTrack(UUID artistUuid, UUID trackId) {
        String stringArtistId = Url62.encode(artistUuid);
        String stringTrackId = Url62.encode(trackId);
        var event = ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.newBuilder()
                .setArtistId(stringArtistId)
                .setTrackId(stringTrackId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringTrackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.WORKFLOW_TOPIC);
    }
}
