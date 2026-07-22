package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;

import java.util.List;

class ArtistPublicControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getArtist_activeArtist_200ReturnsData() {
        ArtistPublicDocument artist = createArtistDoc("Active Artist", EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Active Artist")
                .jsonPath("$.availabilityStatus").isEqualTo("ACTIVE")
                .jsonPath("$.avatarUrl").isNotEmpty();
    }

    @Test
    void getArtist_hiddenArtist_200ReturnsData() {
        ArtistPublicDocument artist = createArtistDoc("Hidden Artist", EntityStatus.HIDDEN);

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Hidden Artist")
                .jsonPath("$.availabilityStatus").isEqualTo("HIDDEN");
    }

    @Test
    void getArtist_deletedArtist_404NotFound() {
        ArtistPublicDocument artist = createArtistDoc("Deleted Artist", EntityStatus.DELETED);

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getArtist_nonExistentId_404NotFound() {
        webTestClient.get()
                .uri("/api/artists/{id}", "non-existent-id")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getArtist_avatarUrlTransformedToCdnUrl() {
        ArtistPublicDocument artist = createArtistDoc("Avatar Test", EntityStatus.ACTIVE);

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.avatarUrl").value(url -> {
                    assert url.toString().startsWith("http://images.localhost:4566/");
                });
    }

    @Test
    void getArtist_socialLinksReturned() {
        ArtistPublicDocument artist = artistRepository.save(ArtistPublicDocument.builder()
                .id(randomId())
                .name("Social Artist")
                .avatarS3Key("avatars/social.jpg")
                .socialLinks(List.of(
                        ArtistPublicDocument.SocialLinkEmbed.builder()
                                .resourceName("instagram")
                                .url("https://instagram.com/social")
                                .build()))
                .tags(List.of())
                .availabilityStatus(EntityStatus.ACTIVE)
                .build()).block();

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.socialLinks[0].resourceName").isEqualTo("instagram")
                .jsonPath("$.socialLinks[0].url").isEqualTo("https://instagram.com/social");
    }

    @Test
    void getArtist_tagsReturned() {
        ArtistPublicDocument artist = artistRepository.save(ArtistPublicDocument.builder()
                .id(randomId())
                .name("Tagged Artist")
                .avatarS3Key("avatars/tagged.jpg")
                .socialLinks(List.of())
                .tags(List.of("jazz", "blues"))
                .availabilityStatus(EntityStatus.ACTIVE)
                .build()).block();

        webTestClient.get()
                .uri("/api/artists/{id}", artist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.tags[0]").isEqualTo("jazz")
                .jsonPath("$.tags[1]").isEqualTo("blues");
    }

    @Test
    void getAlbumsByArtist_onlyReturnsPublishedAndActiveAlbums() {
        ArtistPublicDocument artist = createArtistDoc("Album Artist", EntityStatus.ACTIVE);

        createAlbumDocWithArtist("Published Album", LifecycleStatus.PUBLISHED, EntityStatus.ACTIVE,
                artist.getId(), "Album Artist", ArtistRole.MAIN_ARTIST);
        createAlbumDocWithArtist("Draft Album", LifecycleStatus.CREATED, EntityStatus.ACTIVE,
                artist.getId(), "Album Artist", ArtistRole.MAIN_ARTIST);
        createAlbumDocWithArtist("Deleted Album", LifecycleStatus.PUBLISHED, EntityStatus.DELETED,
                artist.getId(), "Album Artist", ArtistRole.MAIN_ARTIST);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/artists/{id}/albums")
                        .queryParam("sortDirection", "asc")
                        .build(artist.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void getAlbumsByArtist_filtersByType() {
        ArtistPublicDocument artist = createArtistDoc("Type Filter Artist", EntityStatus.ACTIVE);

        albumRepository.save(AlbumPublicDocument.builder()
                .id(randomId())
                .title("Full Album")
                .type(AlbumType.FULL)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(java.time.LocalDateTime.of(2025, 6, 1, 0, 0))
                .year(2025)
                .totalTracks(10)
                .totalDurationMs(300000)
                .coverS3Key("covers/full.jpg")
                .explicit(false)
                .artists(List.of(AlbumPublicDocument.ArtistEmbed.builder()
                        .id(artist.getId())
                        .name("Type Filter Artist")
                        .avatarS3Key("avatars/type-artist.jpg")
                        .role(ArtistRole.MAIN_ARTIST)
                        .build()))
                .build()).block();

        albumRepository.save(AlbumPublicDocument.builder()
                .id(randomId())
                .title("Single Album")
                .type(AlbumType.SINGLE)
                .lifecycleStatus(LifecycleStatus.PUBLISHED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .releaseDate(java.time.LocalDateTime.of(2025, 7, 1, 0, 0))
                .year(2025)
                .totalTracks(1)
                .totalDurationMs(200000)
                .coverS3Key("covers/single.jpg")
                .explicit(false)
                .artists(List.of(AlbumPublicDocument.ArtistEmbed.builder()
                        .id(artist.getId())
                        .name("Type Filter Artist")
                        .avatarS3Key("avatars/type-artist.jpg")
                        .role(ArtistRole.MAIN_ARTIST)
                        .build()))
                .build()).block();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/artists/{id}/albums")
                        .queryParam("albumType", "FULL")
                        .queryParam("sortDirection", "asc")
                        .build(artist.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }

    @Test
    void getAlbumsByArtist_noAlbumsForArtist_200ReturnsEmpty() {
        ArtistPublicDocument artist = createArtistDoc("No Albums Artist", EntityStatus.ACTIVE);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/artists/{id}/albums")
                        .queryParam("sortDirection", "asc")
                        .build(artist.getId()))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(0);
    }
}
