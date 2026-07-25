package org.ultra.rcrs.playlistservice.integration;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
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
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.playlist.AddTracksToPlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.CreatePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeletePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeleteTracksFromPlaylistEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.repository.PlaylistDocumentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.PLAYLIST_COMMANDS_TOPIC
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
    protected PlaylistDocumentRepository playlistRepository;

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    @BeforeEach
    void clearCollections() {
        playlistRepository.deleteAll().block();
    }

    protected static String randomId() {
        return UUID.randomUUID().toString();
    }

    protected PlaylistDocument createPlaylistDoc(String ownerId, String title, boolean isPublic) {
        var now = LocalDateTime.now();
        return playlistRepository.save(PlaylistDocument.builder()
                .id(randomId())
                .ownerId(ownerId)
                .title(title)
                .description("desc for " + title)
                .tags(List.of())
                .coverS3Key("covers/" + title.toLowerCase().replace(" ", "-") + ".jpg")
                .isPublic(isPublic)
                .trackCount(0)
                .tracks(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build()).block();
    }

    protected PlaylistTrack addTrackDoc(String playlistId, String trackId, int position) {
        PlaylistTrack track = PlaylistTrack.builder()
                .trackId(trackId)
                .position(position)
                .addedAt(LocalDateTime.now())
                .build();
        PlaylistDocument playlist = playlistRepository.findById(playlistId).block();
        List<PlaylistTrack> tracks = new ArrayList<>(playlist.getTracks() != null ? playlist.getTracks() : List.of());
        tracks.add(track);
        playlist.setTracks(tracks);
        playlist.setTrackCount(tracks.size());
        playlistRepository.save(playlist).block();
        return track;
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
                .setProducer("playlist-service-test")
                .setPayload(Any.pack(payload))
                .build();
        kafkaTemplate.send(Topics.PLAYLIST_COMMANDS_TOPIC, event.toByteArray()).get();
        Thread.sleep(1000);
    }

    protected void sendCreatePlaylist(String id, String ownerId, String title) throws Exception {
        var event = CreatePlaylistEventOuterClass.CreatePlaylistEvent.newBuilder()
                .setId(id)
                .setOwnerId(ownerId)
                .setTitle(title)
                .setDescription("desc for " + title)
                .setCoverS3Key("covers/" + id + ".jpg")
                .setIsPublic(true)
                .build();
        sendEvent(DomainEventOuterClass.EventType.PLAYLIST_CREATED,
                DomainEventOuterClass.AggregateType.PLAYLIST, id, event);
    }

    protected void sendAddTracksToPlaylist(String playlistId, List<String> trackIds) throws Exception {
        var event = AddTracksToPlaylistEventOuterClass.AddTracksToPlaylistEvent.newBuilder()
                .setPlaylistId(playlistId)
                .addAllTrackIds(trackIds)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACKS_ADDED_TO_PLAYLIST,
                DomainEventOuterClass.AggregateType.PLAYLIST, playlistId, event);
    }

    protected void sendDeleteTracksFromPlaylist(String playlistId, List<String> trackIds) throws Exception {
        var event = DeleteTracksFromPlaylistEventOuterClass.DeleteTracksFromPlaylistEvent.newBuilder()
                .setPlaylistId(playlistId)
                .addAllTrackIds(trackIds)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACKS_REMOVED_FROM_PLAYLIST,
                DomainEventOuterClass.AggregateType.PLAYLIST, playlistId, event);
    }

    protected void sendDeletePlaylist(String id) throws Exception {
        var event = DeletePlaylistEventOuterClass.DeletePlaylistEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.PLAYLIST_DELETED,
                DomainEventOuterClass.AggregateType.PLAYLIST, id, event);
    }
}
