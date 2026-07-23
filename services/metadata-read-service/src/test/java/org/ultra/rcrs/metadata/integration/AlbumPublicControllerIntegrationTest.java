package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import org.ultra.rcrs.enums.ArtistRole;

class AlbumPublicControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAlbum_publishedActiveAlbum_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Published Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Published Album")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE")
                .jsonPath("$.coverUrl").isNotEmpty();
    }

    @Test
    void getAlbum_publishedHiddenAlbum_200ReturnsData() {
        AlbumDocument album = createAlbumDoc("Hidden Album", LifecycleStatus.PUBLISHED, EntityStatus.HIDDEN);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Hidden Album")
                .jsonPath("$.availabilityStatus").isEqualTo("HIDDEN");
    }

    @Test
    void getAlbum_createdLifecycleStatus_404NotFound() {
        AlbumDocument album = createAlbumDoc("Draft Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_transcodingLifecycleStatus_404NotFound() {
        AlbumDocument album = createAlbumDoc("Transcoding Album", LifecycleStatus.TRANSCODING, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_deletedAvailability_404NotFound() {
        AlbumDocument album = createAlbumDoc("Deleted Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/api/albums/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbum_coverUrlTransformedToCdnUrl() {
        AlbumDocument album = createAlbumDoc("Cover Test", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.coverUrl").value(url -> {
                    assert url.toString().startsWith("http://images.localhost:4566/");
                });
    }

    @Test
    void getAlbum_artistsEmbedded() {
        ArtistDocument artist = createArtistDoc("Album Artist", EntityStatus.ACTIVE);
        AlbumDocument album = createAlbumDocWithArtist(
                "Album With Artist", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE,
                artist.getId(), "Album Artist", ArtistRole.MAIN_ARTIST);

        webTestClient.get()
                .uri("/api/albums/{id}", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.artists[0].name").isEqualTo("Album Artist")
                .jsonPath("$.artists[0].role").isEqualTo("MAIN_ARTIST");
    }

    @Test
    void getTracksByAlbum_publishedTracks_200ReturnsTracks() {
        AlbumDocument album = createAlbumDoc("Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Track One", album.getId(), "Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Track Two", album.getId(), "Tracks Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(2);
    }

    @Test
    void getTracksByAlbum_excludesDeletedTracks() {
        AlbumDocument album = createAlbumDoc("Filter Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Active Track", album.getId(), "Filter Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Deleted Track", album.getId(), "Filter Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED);

        webTestClient.get()
                .uri("/api/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void getTracksByAlbum_excludesNonPublishedTracks() {
        AlbumDocument album = createAlbumDoc("Lifecycle Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Published Track", album.getId(), "Lifecycle Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);
        createTrackDoc("Draft Track", album.getId(), "Lifecycle Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void getTracksByAlbum_noTracks_200ReturnsEmpty() {
        AlbumDocument album = createAlbumDoc("Empty Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/albums/{id}/tracks", album.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(0);
    }
}
