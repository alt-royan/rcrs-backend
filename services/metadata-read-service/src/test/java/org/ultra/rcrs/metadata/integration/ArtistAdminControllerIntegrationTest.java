package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.model.ArtistDocument;

import java.util.List;

class ArtistAdminControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getArtist_activeArtist_200ReturnsData() {
        ArtistDocument artist = createArtistDoc("Active Artist", EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/admin/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Active Artist")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE");
    }

    @Test
    void getArtist_deletedArtist_200ReturnsData() {
        ArtistDocument artist = createArtistDoc("Deleted Artist", EntityStatus.DELETED);

        webTestClient.get()
                .uri("/admin/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Deleted Artist")
                .jsonPath("$.availabilityStatus").isEqualTo("DELETED");
    }

    @Test
    void getArtist_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/admin/artists/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAlbumsByArtist_returnsAllAlbumsRegardlessOfStatus() {
        ArtistDocument artist = createArtistDoc("Album Artist", EntityStatus.ACTIVE);
        createAlbumDocWithArtist("Published Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE,
                artist.getId(), "Album Artist", org.ultra.rcrs.enums.ArtistRole.MAIN_ARTIST);
        createAlbumDocWithArtist("Created Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE,
                artist.getId(), "Album Artist", org.ultra.rcrs.enums.ArtistRole.MAIN_ARTIST);
        createAlbumDocWithArtist("Deleted Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED,
                artist.getId(), "Album Artist", org.ultra.rcrs.enums.ArtistRole.MAIN_ARTIST);

        webTestClient.get()
                .uri("/admin/artists/{id}/albums", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(3);
    }

    @Test
    void getAlbumsByArtist_filtersByType() {
        ArtistDocument artist = createArtistDoc("Type Artist", EntityStatus.ACTIVE);

        var albumRepo = albumRepository;
        albumRepo.save(AlbumDocument.builder()
                .id(randomId())
                .title("Full Album")
                .type(org.ultra.rcrs.enums.AlbumType.FULL)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(java.time.LocalDateTime.of(2025, 6, 1, 0, 0))
                .year(2025)
                .totalTracks(10)
                .totalDurationMs(300000)
                .coverS3Key("covers/full.jpg")
                .explicit(false)
                .artists(List.of(AlbumDocument.ArtistEmbed.builder()
                        .id(artist.getId())
                        .name("Type Artist")
                        .avatarS3Key("avatars/type-artist.jpg")
                        .role(org.ultra.rcrs.enums.ArtistRole.MAIN_ARTIST)
                        .build()))
                .build()).block();

        albumRepo.save(AlbumDocument.builder()
                .id(randomId())
                .title("Single Album")
                .type(org.ultra.rcrs.enums.AlbumType.SINGLE)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(java.time.LocalDateTime.of(2025, 7, 1, 0, 0))
                .year(2025)
                .totalTracks(1)
                .totalDurationMs(200000)
                .coverS3Key("covers/single.jpg")
                .explicit(false)
                .artists(List.of(AlbumDocument.ArtistEmbed.builder()
                        .id(artist.getId())
                        .name("Type Artist")
                        .avatarS3Key("avatars/type-artist.jpg")
                        .role(org.ultra.rcrs.enums.ArtistRole.MAIN_ARTIST)
                        .build()))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/artists/{id}/albums")
                        .queryParam("type", "FULL")
                        .build(artist.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void countArtists_filtersByAvailabilityStatus() {
        createArtistDoc("Active Artist", EntityStatus.ACTIVE);
        createArtistDoc("Deleted Artist", EntityStatus.DELETED);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/artists/count")
                        .queryParam("availabilityStatus", "ACTIVE")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Long.class).isEqualTo(1L);
    }
}
