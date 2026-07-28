package org.ultra.rcrs.playlistservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistType;
import org.ultra.rcrs.playlistservice.repository.PlaylistRepository;
import org.ultra.rcrs.playlistservice.repository.PlaylistTrackRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Playlists are REST-only: the Kafka command path was removed, so nothing here
 * publishes events. {@code @EmbeddedKafka} stays purely because {@code KafkaConfig}
 * is still on the classpath and its beans need a broker address to start.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
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
}
