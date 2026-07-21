package org.ultra.rcrs.metadata.controller;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.config.EnableReactiveMongoRepositories;
import org.springframework.data.mongodb.core.AbstractReactiveMongoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.enums.*;
import org.ultra.rcrs.metadata.config.MongoReactiveConfig;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = MongoReactiveConfig.class
))
@AutoConfigureWebTestClient
@Testcontainers
@ActiveProfiles("test")
class RestEndpointIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired private WebTestClient webTestClient;
    @Autowired private ArtistDocumentRepository artistDocRepository;
    @Autowired private AlbumDocumentRepository albumDocRepository;
    @Autowired private TrackDocumentRepository trackDocRepository;

    private String artistId;
    private String albumId;
    private String trackId;

    @BeforeEach
    void setUp() {
        artistDocRepository.deleteAll().block();
        albumDocRepository.deleteAll().block();
        trackDocRepository.deleteAll().block();

        ArtistPublicDocument artist = artistDocRepository.save(ArtistPublicDocument.builder()
                .id("rest-artist-001")
                .name("REST Test Artist")
                .avatarS3Key("avatar.jpg")
                .availabilityStatus(EntityStatus.ACTIVE)
                .tags(List.of("jazz", "blues"))
                .socialLinks(List.of(
                        ArtistPublicDocument.SocialLinkEmbed.builder()
                                .resourceName("website")
                                .url("https://example.com")
                                .build()))
                .build()).block();
        artistId = artist.getId();

        AlbumPublicDocument album = albumDocRepository.save(AlbumPublicDocument.builder()
                .id("rest-album-001")
                .title("REST Test Album")
                .type(AlbumType.FULL)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(LocalDateTime.of(2026, 3, 15, 0, 0))
                .year(2026)
                .totalTracks(2)
                .totalDurationMs(420000)
                .coverS3Key("cover.jpg")
                .artists(List.of(
                        AlbumPublicDocument.ArtistEmbed.builder()
                                .id(artistId)
                                .name("REST Test Artist")
                                .avatarS3Key("avatar.jpg")
                                .role(ArtistRole.MAIN_ARTIST)
                                .build()))
                .build()).block();
        albumId = album.getId();

        TrackPublicDocument track = trackDocRepository.save(TrackPublicDocument.builder()
                .id("rest-track-001")
                .title("REST Test Track")
                .trackNumber(1)
                .explicit(false)
                .durationMs(210000)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(LocalDateTime.of(2026, 3, 15, 0, 0))
                .album(TrackPublicDocument.AlbumEmbed.builder()
                        .id(albumId)
                        .title("REST Test Album")
                        .coverS3Key("cover.jpg")
                        .build())
                .artists(List.of(
                        TrackPublicDocument.ArtistEmbed.builder()
                                .id(artistId)
                                .name("REST Test Artist")
                                .avatarS3Key("avatar.jpg")
                                .role(ArtistRole.MAIN_ARTIST)
                                .build()))
                .build()).block();
        trackId = track.getId();
    }

    @Test
    void getArtist_returnsCorrectData() {
        webTestClient.get()
                .uri("/api/artists/{artistId}", artistId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(artistId)
                .jsonPath("$.name").isEqualTo("REST Test Artist")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE")
                .jsonPath("$.tags[0]").isEqualTo("jazz")
                .jsonPath("$.tags[1]").isEqualTo("blues")
                .jsonPath("$.socialLinks[0].resourceName").isEqualTo("website");
    }

    @Test
    void getArtist_notFound_returns404() {
        webTestClient.get()
                .uri("/api/artists/{artistId}", "nonexistent-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_returnsCorrectData() {
        webTestClient.get()
                .uri("/api/albums/{albumId}", albumId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(albumId)
                .jsonPath("$.title").isEqualTo("REST Test Album")
                .jsonPath("$.type").isEqualTo("FULL")
                .jsonPath("$.totalTracks").isEqualTo(2)
                .jsonPath("$.totalDurationMs").isEqualTo(420000)
                .jsonPath("$.artists[0].id").isEqualTo(artistId)
                .jsonPath("$.artists[0].role").isEqualTo("MAIN_ARTIST");
    }

    @Test
    void getAlbum_notFound_returns404() {
        webTestClient.get()
                .uri("/api/albums/{albumId}", "nonexistent-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_hidden_returnsData() {
        albumDocRepository.findById(albumId).flatMap(doc -> {
            doc.setAvailabilityStatus(EntityStatus.HIDDEN);
            return albumDocRepository.save(doc);
        }).block();

        webTestClient.get()
                .uri("/api/albums/{albumId}", albumId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(albumId);
    }

    @Test
    void getAlbum_deleted_returns404() {
        albumDocRepository.findById(albumId).flatMap(doc -> {
            doc.setAvailabilityStatus(EntityStatus.DELETED);
            return albumDocRepository.save(doc);
        }).block();

        webTestClient.get()
                .uri("/api/albums/{albumId}", albumId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_notPublished_returns404() {
        albumDocRepository.findById(albumId).flatMap(doc -> {
            doc.setLifecycleStatus(LifecycleStatus.CREATED);
            return albumDocRepository.save(doc);
        }).block();

        webTestClient.get()
                .uri("/api/albums/{albumId}", albumId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_returnsCorrectData() {
        webTestClient.get()
                .uri("/api/tracks/{trackId}", trackId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(trackId)
                .jsonPath("$.title").isEqualTo("REST Test Track")
                .jsonPath("$.trackNumber").isEqualTo(1)
                .jsonPath("$.explicit").isEqualTo(false)
                .jsonPath("$.durationMs").isEqualTo(210000)
                .jsonPath("$.album.id").isEqualTo(albumId)
                .jsonPath("$.album.title").isEqualTo("REST Test Album")
                .jsonPath("$.artists[0].id").isEqualTo(artistId)
                .jsonPath("$.artists[0].role").isEqualTo("MAIN_ARTIST");
    }

    @Test
    void getTrack_notFound_returns404() {
        webTestClient.get()
                .uri("/api/tracks/{trackId}", "nonexistent-id")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTracksByAlbum_returnsCorrectList() {
        trackDocRepository.save(TrackPublicDocument.builder()
                .id("rest-track-002")
                .title("Track Two")
                .trackNumber(2)
                .explicit(true)
                .durationMs(210000)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .album(TrackPublicDocument.AlbumEmbed.builder()
                        .id(albumId)
                        .title("REST Test Album")
                        .coverS3Key("cover.jpg")
                        .build())
                .build()).block();

        webTestClient.get()
                .uri("/api/albums/{albumId}/tracks", albumId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(2);
    }

    @Test
    void getAlbumsByArtist_returnsCorrectList() {
        webTestClient.get()
                .uri("/api/artists/{artistId}/albums", artistId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .hasSize(1)
                .value(list -> {
                    var album = (java.util.Map<?, ?>) list.get(0);
                    assertThat(album.get("id")).isEqualTo(albumId);
                    assertThat(album.get("title")).isEqualTo("REST Test Album");
                });
    }
}
