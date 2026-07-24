package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.model.TrackDocument;

class TrackAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getTrack_createdLifecycleStatus_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Draft Track", album.getId(), "Track Album",
                LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Draft Track")
                .jsonPath("$.lifecycleStatus").isEqualTo("CREATED")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE");
    }

    @Test
    void getTrack_deletedAvailability_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Deleted Track", album.getId(), "Track Album",
                LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/catalog/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Deleted Track")
                .jsonPath("$.availabilityStatus").isEqualTo("DELETED");
    }

    @Test
    void getTrack_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/catalog/admin/tracks/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_lifecycleStatusFieldExposed() {
        AlbumDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Lifecycle Track", album.getId(), "Track Album",
                LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lifecycleStatus").isEqualTo("TRANSCODING");
    }

    @Test
    void getTrack_albumEmbedIncluded() {
        AlbumDocument album = createAlbumDoc("Embed Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackDocument track = createTrackDoc("Embed Track", album.getId(), "Embed Album",
                LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/catalog/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.album.id").isEqualTo(album.getId())
                .jsonPath("$.album.title").isEqualTo("Embed Album");
    }

    @Test
    void countTracks_filtersByAlbumId() {
        AlbumDocument albumA = createAlbumDoc("Album A", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        AlbumDocument albumB = createAlbumDoc("Album B", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        createTrackDoc("Track A1", albumA.getId(), "Album A", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Track A2", albumA.getId(), "Album A", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Track B1", albumB.getId(), "Album B", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/catalog/admin/tracks/count")
                        .queryParam("albumId", albumA.getId())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).isEqualTo(2L);
    }
}
