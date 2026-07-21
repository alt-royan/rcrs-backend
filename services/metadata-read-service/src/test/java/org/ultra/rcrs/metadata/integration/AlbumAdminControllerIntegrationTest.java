package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;

import java.util.List;

class AlbumAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAlbum_createdLifecycleStatus_200ReturnsData() {
        AlbumPublicDocument album = createAlbumDoc("Draft Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Draft Album")
                .jsonPath("$.lifecycleStatus").isEqualTo("CREATED")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE");
    }

    @Test
    void getAlbum_deletedAvailability_200ReturnsData() {
        AlbumPublicDocument album = createAlbumDoc("Deleted Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/admin/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Deleted Album")
                .jsonPath("$.availabilityStatus").isEqualTo("DELETED");
    }

    @Test
    void getAlbum_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/admin/albums/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_lifecycleStatusFieldExposed() {
        AlbumPublicDocument album = createAlbumDoc("Lifecycle Album", LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.lifecycleStatus").isEqualTo("TRANSCODING");
    }

    @Test
    void getTracksByAlbum_returnsAllTracksRegardlessOfStatus() {
        AlbumPublicDocument album = createAlbumDoc("Admin Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Published Track", album.getId(), "Admin Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Created Track", album.getId(), "Admin Tracks Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE);
        createTrackDoc("Deleted Track", album.getId(), "Admin Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/admin/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(3);
    }

    @Test
    void getTracksByAlbum_noTracks_200ReturnsEmpty() {
        AlbumPublicDocument album = createAlbumDoc("Empty Admin Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(0);
    }

    @Test
    void getTracksByAlbum_lifecycleStatusFieldExposed() {
        AlbumPublicDocument album = createAlbumDoc("Status Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Track With Status", album.getId(), "Status Album", LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1)
                .value(list -> {
                    var track = (java.util.Map<?, ?>) list.get(0);
                    assert track.get("lifecycleStatus").equals("TRANSCODING");
                });
    }
}
