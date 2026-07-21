package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
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
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude=org.ultra.rcrs.metadata.config.MongoReactiveConfig,org.ultra.rcrs.metadata.config.KafkaConfig",
        "spring.kafka.bootstrap-servers=localhost:9093"
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
}
