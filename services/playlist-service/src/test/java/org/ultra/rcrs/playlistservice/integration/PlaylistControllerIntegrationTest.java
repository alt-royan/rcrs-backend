package org.ultra.rcrs.playlistservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.ultra.rcrs.playlistservice.dto.request.CreatePlaylistRequest;
import org.ultra.rcrs.playlistservice.dto.request.TrackIdsRequest;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaylistControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createPlaylist_201CreatedAndPersisted() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTitle("My Mix");
        request.setDescription("chill vibes");
        request.setPublic(true);

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("user-1")))
                .post()
                .uri("/playlists")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNotEmpty();
    }

    @Test
    void createPlaylist_missingTitle_400BadRequest() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setDescription("no title");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt().jwt(jwt -> jwt.subject("user-1")))
                .post()
                .uri("/playlists")
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getPlaylists_byIds_returnsMatchingPlaylistsWithoutTracks() {
        PlaylistDocument p1 = createPlaylistDoc("user-1", "Playlist One", true);
        PlaylistDocument p2 = createPlaylistDoc("user-1", "Playlist Two", false);
        createPlaylistDoc("user-1", "Playlist Three (excluded)", true);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/playlists")
                        .queryParam("ids", p1.getId(), p2.getId())
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(2);
    }

    @Test
    void getTracks_pagination_respectsOffsetAndLimit() {
        PlaylistDocument playlist = createPlaylistDoc("user-1", "Paginated", true);
        for (int i = 0; i < 5; i++) {
            addTrackDoc(playlist.getId(), "track-" + i, i);
        }

        webTestClient.get()
                .uri("/playlists/{id}/tracks?offset=2&limit=2", playlist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(2);
    }

    @Test
    void addTracks_thenGetTracks_returnsAppendedTracksInOrder() {
        PlaylistDocument playlist = createPlaylistDoc("user-1", "Add Tracks", true);
        TrackIdsRequest request = new TrackIdsRequest();
        request.setTrackIds(List.of("track-a", "track-b"));

        webTestClient.put()
                .uri("/playlists/{id}/tracks", playlist.getId())
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        var doc = playlistRepository.findById(playlist.getId()).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTrackCount()).isEqualTo(2);

        webTestClient.get()
                .uri("/playlists/{id}/tracks", playlist.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(2);
    }

    @Test
    void deleteTracks_removesOnlyRequestedTracks() {
        PlaylistDocument playlist = createPlaylistDoc("user-1", "Remove Tracks", true);
        addTrackDoc(playlist.getId(), "track-a", 0);
        addTrackDoc(playlist.getId(), "track-b", 1);
        playlist.setTrackCount(2);
        playlist.setNextPosition(2);
        playlistRepository.save(playlist).block();

        TrackIdsRequest request = new TrackIdsRequest();
        request.setTrackIds(List.of("track-a"));

        webTestClient.method(org.springframework.http.HttpMethod.DELETE)
                .uri("/playlists/{id}/tracks", playlist.getId())
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();

        var remaining = playlistTrackRepository.findAllByPlaylistIdAndTrackIdIn(playlist.getId(), List.of("track-a", "track-b"))
                .collectList().block();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getTrackId()).isEqualTo("track-b");

        var doc = playlistRepository.findById(playlist.getId()).block();
        assertThat(doc.getTrackCount()).isEqualTo(1);
    }

    @Test
    void deletePlaylist_removesPlaylistAndItsTracks() {
        PlaylistDocument playlist = createPlaylistDoc("user-1", "To Delete", true);
        addTrackDoc(playlist.getId(), "track-a", 0);

        webTestClient.delete()
                .uri("/playlists/{id}", playlist.getId())
                .exchange()
                .expectStatus().isNoContent();

        assertThat(playlistRepository.findById(playlist.getId()).block()).isNull();
        assertThat(playlistTrackRepository.findAllByPlaylistIdAndTrackIdIn(playlist.getId(), List.of("track-a"))
                .collectList().block()).isEmpty();
    }
}
