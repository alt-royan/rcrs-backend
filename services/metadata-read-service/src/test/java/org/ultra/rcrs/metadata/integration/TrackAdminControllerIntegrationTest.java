package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;

class TrackAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getTrack_createdLifecycleStatus_200ReturnsData() {
        AlbumPublicDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackPublicDocument track = createTrackDoc("Draft Track", album.getId(), "Track Album",
                LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Draft Track")
                .jsonPath("$.lifecycleStatus").isEqualTo("CREATED")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE");
    }

    @Test
    void getTrack_deletedAvailability_200ReturnsData() {
        AlbumPublicDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackPublicDocument track = createTrackDoc("Deleted Track", album.getId(), "Track Album",
                LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Deleted Track")
                .jsonPath("$.availabilityStatus").isEqualTo("DELETED");
    }

    @Test
    void getTrack_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/admin/tracks/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getTrack_lifecycleStatusFieldExposed() {
        AlbumPublicDocument album = createAlbumDoc("Track Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackPublicDocument track = createTrackDoc("Lifecycle Track", album.getId(), "Track Album",
                LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lifecycleStatus").isEqualTo("TRANSCODING");
    }

    @Test
    void getTrack_albumEmbedIncluded() {
        AlbumPublicDocument album = createAlbumDoc("Embed Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        TrackPublicDocument track = createTrackDoc("Embed Track", album.getId(), "Embed Album",
                LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/tracks/{id}", track.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.album.id").isEqualTo(album.getId())
                .jsonPath("$.album.title").isEqualTo("Embed Album");
    }
}
