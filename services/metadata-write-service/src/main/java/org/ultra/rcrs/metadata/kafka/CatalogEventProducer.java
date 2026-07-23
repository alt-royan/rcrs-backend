package org.ultra.rcrs.metadata.kafka;

import com.google.protobuf.Any;
import com.google.protobuf.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.*;
import org.ultra.rcrs.events.artist.*;
import org.ultra.rcrs.events.common.*;
import org.ultra.rcrs.events.track.*;
import org.ultra.rcrs.kafka.ProtobufEventProducer;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.OtherArtist;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Component
public class CatalogEventProducer extends ProtobufEventProducer {

    @Value("${spring.application.name}")
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
            Instant instant = album.getReleaseDate().toInstant(ZoneOffset.UTC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void albumDeleted(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = AlbumDeletedEventOuterClass.AlbumDeletedEvent.newBuilder()
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void albumHidden(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = AlbumHiddenEventOuterClass.AlbumHiddenEvent.newBuilder()
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackCreated(Track track) {
        String trackId = Url62.encode(track.getId());

        var eventBuilder = TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                .setId(trackId)
                .setTitle(track.getTitle())
                .setTrackNumber(track.getTrackNumber())
                .setExplicit(track.getExplicit())
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(track.getAvailabilityStatus().name()))
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(track.getLifecycleStatus().name()));

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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackAddedToAlbum(UUID trackId, UUID albumId) {
        String stringTrackId = Url62.encode(trackId);
        String stringAlbumId = Url62.encode(albumId);
        var event = TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.newBuilder()
                .setTrackId(stringTrackId)
                .setAlbumId(stringAlbumId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringTrackId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackDeleted(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = TrackDeletedEventOuterClass.TrackDeletedEvent.newBuilder()
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackHidden(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = TrackHiddenEventOuterClass.TrackHiddenEvent.newBuilder()
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void otherAddedToTrack(OtherArtist other, UUID trackUuid) {
        String stringOtherId = Url62.encode(other.getId());
        String stringTrackId = Url62.encode(trackUuid);
        Set<ArtistRole> roles = other.getRoles() != null ? other.getRoles() : new HashSet<>();
        var builder = OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.newBuilder()
                .setOtherId(stringOtherId)
                .setTrackId(stringTrackId)
                .setName(other.getName())
                .addAllRoles(roles.stream()
                        .map(r -> ArtistRoleOuterClass.ArtistRole.valueOf(r.name())).toList());
        if (other.getSocialLinks() != null) {
            builder.addAllSocialLinks(other.getSocialLinks().getItems().stream()
                    .map(link -> SocialLinkOuterClass.SocialLink.newBuilder()
                            .setResourceName(link.getResourceName())
                            .setUrl(link.getUrl()).build()).toList());
        }
        var event = builder.build();

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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
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
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void updateAlbumLifecycleStatus(LifecycleStatus status, UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.newBuilder()
                .setId(stringId)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void updateTrackLifecycleStatus(LifecycleStatus status, UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.newBuilder()
                .setId(stringId)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void artistActivated(UUID artistId) {
        String stringId = Url62.encode(artistId);
        var event = ArtistActivatedEventOuterClass.ArtistActivatedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_ACTIVATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void albumActivated(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = AlbumActivatedEventOuterClass.AlbumActivatedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_ACTIVATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackActivated(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = TrackActivatedEventOuterClass.TrackActivatedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_ACTIVATED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void artistTrueDeleted(UUID artistId) {
        String stringId = Url62.encode(artistId);
        var event = ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ARTIST_TRUE_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ARTIST)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void albumTrueDeleted(UUID albumId) {
        String stringId = Url62.encode(albumId);
        var event = AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.ALBUM_TRUE_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.ALBUM)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }

    public void trackTrueDeleted(UUID trackId) {
        String stringId = Url62.encode(trackId);
        var event = TrackTrueDeletedEventOuterClass.TrackTrueDeletedEvent.newBuilder()
                .setId(stringId)
                .build();

        var now = Instant.now();
        var domainEvent = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(DomainEventOuterClass.EventType.TRACK_TRUE_DELETED)
                .setAggregateType(DomainEventOuterClass.AggregateType.TRACK)
                .setAggregateId(stringId)
                .setOccurredAt(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
                .setProducer(serviceName)
                .setPayload(Any.pack(event))
                .build();
        sendEvent(domainEvent, Topics.CATALOG_CDC_TOPIC);
        sendEvent(domainEvent, Topics.SEARCH_INDEX_TOPIC);
    }
}
