package org.ultra.rcrs.metadata.integration;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.*;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.common.*;
import org.ultra.rcrs.events.track.*;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.CATALOG_CDC_TOPIC,
        Topics.SEARCH_INDEX_TOPIC
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DirtiesContext
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Autowired
    protected WebTestClient webTestClient;

    @Autowired
    protected ArtistDocumentRepository artistRepository;

    @Autowired
    protected AlbumDocumentRepository albumRepository;

    @Autowired
    protected TrackDocumentRepository trackRepository;

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    @BeforeEach
    void clearCollections() {
        artistRepository.deleteAll().block();
        albumRepository.deleteAll().block();
        trackRepository.deleteAll().block();
    }

    protected ArtistPublicDocument createArtistDoc(String name, EntityStatus status) {
        return artistRepository.save(ArtistPublicDocument.builder()
                .id(randomId())
                .name(name)
                .avatarS3Key("avatars/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .socialLinks(List.of())
                .tags(List.of("rock"))
                .availabilityStatus(status)
                .build()).block();
    }

    protected AlbumPublicDocument createAlbumDoc(String title, LifecycleStatus lifecycle,
                                                 EntityStatus availability) {
        return albumRepository.save(AlbumPublicDocument.builder()
                .id(randomId())
                .title(title)
                .type(AlbumType.FULL)
                .lifecycleStatus(lifecycle)
                .availabilityStatus(availability)
                .releaseDate(LocalDateTime.of(2025, 1, 15, 0, 0))
                .year(2025)
                .totalTracks(10)
                .totalDurationMs(300000)
                .coverS3Key("covers/" + title.toLowerCase().replace(" ", "-") + ".jpg")
                .explicit(false)
                .artists(List.of())
                .build()).block();
    }

    protected AlbumPublicDocument createAlbumDocWithArtist(String title, LifecycleStatus lifecycle,
                                                           EntityStatus availability,
                                                           String artistId, String artistName,
                                                           ArtistRole role) {
        return albumRepository.save(AlbumPublicDocument.builder()
                .id(randomId())
                .title(title)
                .type(AlbumType.FULL)
                .lifecycleStatus(lifecycle)
                .availabilityStatus(availability)
                .releaseDate(LocalDateTime.of(2025, 1, 15, 0, 0))
                .year(2025)
                .totalTracks(10)
                .totalDurationMs(300000)
                .coverS3Key("covers/" + title.toLowerCase().replace(" ", "-") + ".jpg")
                .explicit(false)
                .artists(List.of(AlbumPublicDocument.ArtistEmbed.builder()
                        .id(artistId)
                        .name(artistName)
                        .avatarS3Key("avatars/" + artistId + ".jpg")
                        .role(role)
                        .build()))
                .build()).block();
    }

    protected TrackPublicDocument createTrackDoc(String title, String albumId, String albumTitle,
                                                 LifecycleStatus lifecycle, EntityStatus availability) {
        return trackRepository.save(TrackPublicDocument.builder()
                .id(randomId())
                .title(title)
                .trackNumber(1)
                .durationMs(240000)
                .explicit(false)
                .lifecycleStatus(lifecycle)
                .availabilityStatus(availability)
                .album(TrackPublicDocument.AlbumEmbed.builder()
                        .id(albumId)
                        .title(albumTitle)
                        .coverS3Key("covers/" + albumId + ".jpg")
                        .build())
                .artists(List.of())
                .others(List.of())
                .build()).block();
    }

    protected static String randomId() {
        return UUID.randomUUID().toString();
    }

    protected void sendEvent(DomainEventOuterClass.EventType eventType,
                             DomainEventOuterClass.AggregateType aggregateType,
                             String aggregateId,
                             GeneratedMessage payload) throws ExecutionException, InterruptedException {
        DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(eventType)
                .setAggregateType(aggregateType)
                .setAggregateId(aggregateId)
                .setOccurredAt(Timestamp.newBuilder()
                        .setSeconds(System.currentTimeMillis() / 1000)
                        .build())
                .setProducer("metadata-read-service-test")
                .setPayload(Any.pack(payload))
                .build();
        kafkaTemplate.send(Topics.CATALOG_CDC_TOPIC, event.toByteArray()).get();
        Thread.sleep(2000);
    }

    protected void sendArtistCreated(String id, String name, EntityStatus status) throws ExecutionException, InterruptedException {
        var event = ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                .setId(id)
                .setName(name)
                .setAvatarS3Key("avatars/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .addTags("rock")
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.valueOf(status.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_CREATED,
                DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    protected void sendArtistDeleted(String id) throws ExecutionException, InterruptedException {
        var event = org.ultra.rcrs.events.artist.ArtistDeletedEventOuterClass.ArtistDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED,
                DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    protected void sendArtistHidden(String id) throws Exception {
        var event = org.ultra.rcrs.events.artist.ArtistHiddenEventOuterClass.ArtistHiddenEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_HIDDEN,
                DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    protected void sendArtistActivated(String id) throws Exception {
        var event = org.ultra.rcrs.events.artist.ArtistActivatedEventOuterClass.ArtistActivatedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ACTIVATED,
                DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    protected void sendArtistTrueDeleted(String id) throws Exception {
        var event = org.ultra.rcrs.events.artist.ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_TRUE_DELETED,
                DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    protected void sendAlbumCreated(String id, String title) throws Exception {
        var event = AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                .setId(id)
                .setTitle(title)
                .setType(AlbumTypeOuterClass.AlbumType.FULL)
                .setCoverS3Key("covers/" + id + ".jpg")
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_CREATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendAlbumDeleted(String id) throws Exception {
        var event = AlbumDeletedEventOuterClass.AlbumDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_DELETED,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendAlbumHidden(String id) throws Exception {
        var event = AlbumHiddenEventOuterClass.AlbumHiddenEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_HIDDEN,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendAlbumActivated(String id) throws Exception {
        var event = AlbumActivatedEventOuterClass.AlbumActivatedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_ACTIVATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendAlbumTrueDeleted(String id) throws Exception {
        var event = AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_TRUE_DELETED,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendAlbumLifecycleStatusUpdated(String id, LifecycleStatus status) throws Exception {
        var event = AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.newBuilder()
                .setId(id)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, event);
    }

    protected void sendArtistAddedToAlbum(String artistId, String albumId, ArtistRole role) throws Exception {
        var event = ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                .setArtistId(artistId)
                .setAlbumId(albumId)
                .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM,
                DomainEventOuterClass.AggregateType.ALBUM, albumId, event);
    }

    protected void sendArtistDeletedFromAlbum(String artistId, String albumId) throws Exception {
        var event = ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.newBuilder()
                .setArtistId(artistId)
                .setAlbumId(albumId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM,
                DomainEventOuterClass.AggregateType.ALBUM, albumId, event);
    }

    protected void sendTrackCreated(String id, String title) throws Exception {
        var event = TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                .setId(id)
                .setTitle(title)
                .setTrackNumber(1)
                .setExplicit(false)
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_CREATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackDeleted(String id) throws Exception {
        var event = TrackDeletedEventOuterClass.TrackDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_DELETED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackHidden(String id) throws Exception {
        var event = TrackHiddenEventOuterClass.TrackHiddenEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_HIDDEN,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackActivated(String id) throws Exception {
        var event = TrackActivatedEventOuterClass.TrackActivatedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_ACTIVATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackTrueDeleted(String id) throws Exception {
        var event = TrackTrueDeletedEventOuterClass.TrackTrueDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_TRUE_DELETED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackLifecycleStatusUpdated(String id, LifecycleStatus status) throws Exception {
        var event = TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.newBuilder()
                .setId(id)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    protected void sendTrackAddedToAlbum(String trackId, String albumId) throws Exception {
        var event = TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.newBuilder()
                .setTrackId(trackId)
                .setAlbumId(albumId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    protected void sendArtistAddedToTrack(String artistId, String trackId, ArtistRole role) throws Exception {
        var event = ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    protected void sendArtistDeletedFromTrack(String artistId, String trackId) throws Exception {
        var event = ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    protected void sendOtherAddedToTrack(String otherId, String trackId, String name) throws Exception {
        var event = OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.newBuilder()
                .setOtherId(otherId)
                .setTrackId(trackId)
                .setName(name)
                .addRoles(ArtistRoleOuterClass.ArtistRole.FEATURED_ARTIST)
                .build();
        sendEvent(DomainEventOuterClass.EventType.OTHER_ADDED_TO_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    protected void sendOtherDeletedFromTrack(String otherId, String trackId) throws Exception {
        var event = OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent.newBuilder()
                .setOtherId(otherId)
                .setTrackId(trackId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.OTHER_DELETED_FROM_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }
}
