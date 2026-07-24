package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.model.TrackDocument;

import java.util.List;

class TrackPublicControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getTrack_publishedActiveTrack_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Published Track", album.getId(), "Track Album",
                LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Published Track")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE")
                .jsonPath("$.trackNumber").isEqualTo(1)
                .jsonPath("$.durationMs").isEqualTo(240000);
    }

    @Test
    void getTrack_publishedHiddenTrack_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Hidden Track", album.getId(), "Track Album",
                LifecycleStatus.PUBLISHED, EntityStatus.HIDDEN);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Hidden Track")
                .jsonPath("$.availabilityStatus").isEqualTo("HIDDEN");
    }

    @Test
    void getTrack_createdLifecycleStatus_404NotFound() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Draft Track", album.getId(), "Track Album",
                LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_transcodingLifecycleStatus_404NotFound() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Transcoding Track", album.getId(), "Track Album",
                LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_deletedAvailability_404NotFound() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Deleted Track", album.getId(), "Track Album",
                LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/catalog/tracks/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_albumEmbedIncluded() {
        AlbumDocument album = createAlbumDoc("Embed Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Embed Track", album.getId(), "Embed Album",
                LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.album.id").isEqualTo(album.getId())
                .jsonPath("$.album.title").isEqualTo("Embed Album")
                .jsonPath("$.album.coverUrl").isNotEmpty();
    }

    @Test
    void getTrack_explicitFieldReturned() {
        AlbumDocument album = createAlbumDoc("Explicit Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = trackRepository.save(TrackDocument.builder()
                .id(randomId())
                .title("Explicit Track")
                .trackNumber(1)
                .durationMs(240000)
                .explicit(true)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .album(TrackDocument.AlbumEmbed.builder()
                        .id(album.getId())
                        .title("Explicit Album")
                        .coverS3Key("covers/explicit.jpg")
                        .build())
                .artists(List.of())
                .others(List.of())
                .build()).block();

        webTestClient.get()
                .uri("/catalog/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.explicit").isEqualTo(true);
    }
}
