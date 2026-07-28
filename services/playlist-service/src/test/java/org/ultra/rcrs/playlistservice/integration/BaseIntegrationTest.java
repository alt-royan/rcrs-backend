package org.ultra.rcrs.playlistservice.integration;

import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.PlaylistTypeOuterClass;
import org.ultra.rcrs.events.playlist.AddTracksToPlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.CreatePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeletePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeleteTracksFromPlaylistEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistType;
import org.ultra.rcrs.playlistservice.repository.PlaylistRepository;
import org.ultra.rcrs.playlistservice.repository.PlaylistTrackRepository;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
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
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PlaylistRepository playlistRepository;

    @Autowired
    protected PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    @BeforeEach
    void clearData() {
        playlistTrackRepository.deleteAll();
        playlistRepository.deleteAll();
    }

    protected static UUID randomId() {
        return UUID.randomUUID();
    }

    protected Playlist createPlaylistDoc(String ownerId, String title, boolean isPrivate) {
        var now = Instant.now();
        return playlistRepository.save(Playlist.builder()
                .id(randomId())
                .ownerId(ownerId)
                .title(title)
                .description("desc for " + title)
                .tags(List.of())
                .coverS3Key("covers/" + title.toLowerCase().replace(" ", "-") + ".jpg")
                .isPrivate(isPrivate)
                .type(PlaylistType.CUSTOM)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    /**
     * Positions are 1-based, matching {@code PlaylistService}.
     */
    protected PlaylistTrack addTrackDoc(UUID playlistId, String trackId, int position) {
        return playlistTrackRepository.save(PlaylistTrack.builder()
                .playlistId(playlistId)
                .trackId(trackId)
                .position(position)
                .addedAt(Instant.now())
                .build());
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

    protected void sendCreatePlaylist(UUID id, String ownerId, String title) throws Exception {
        String encodedId = Url62.encode(id);
        var event = CreatePlaylistEventOuterClass.CreatePlaylistEvent.newBuilder()
                .setId(encodedId)
                .setOwnerId(ownerId)
                .setTitle(title)
                .setDescription("desc for " + title)
                .setCoverS3Key("covers/" + encodedId + ".jpg")
                .setIsPrivate(true)
                .setType(PlaylistTypeOuterClass.PlaylistType.CUSTOM)
                .build();
        sendEvent(DomainEventOuterClass.EventType.PLAYLIST_CREATED,
                DomainEventOuterClass.AggregateType.PLAYLIST, encodedId, event);
    }

    protected void sendAddTracksToPlaylist(UUID playlistId, List<String> trackIds) throws Exception {
        String encodedId = Url62.encode(playlistId);
        var event = AddTracksToPlaylistEventOuterClass.AddTracksToPlaylistEvent.newBuilder()
                .setPlaylistId(encodedId)
                .addAllTrackIds(trackIds)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACKS_ADDED_TO_PLAYLIST,
                DomainEventOuterClass.AggregateType.PLAYLIST, encodedId, event);
    }

    protected void sendDeleteTracksFromPlaylist(UUID playlistId, List<String> trackIds) throws Exception {
        String encodedId = Url62.encode(playlistId);
        var event = DeleteTracksFromPlaylistEventOuterClass.DeleteTracksFromPlaylistEvent.newBuilder()
                .setPlaylistId(encodedId)
                .addAllTrackIds(trackIds)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACKS_REMOVED_FROM_PLAYLIST,
                DomainEventOuterClass.AggregateType.PLAYLIST, encodedId, event);
    }

    protected void sendDeletePlaylist(UUID id) throws Exception {
        String encodedId = Url62.encode(id);
        var event = DeletePlaylistEventOuterClass.DeletePlaylistEvent.newBuilder()
                .setId(encodedId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.PLAYLIST_DELETED,
                DomainEventOuterClass.AggregateType.PLAYLIST, encodedId, event);
    }
}
