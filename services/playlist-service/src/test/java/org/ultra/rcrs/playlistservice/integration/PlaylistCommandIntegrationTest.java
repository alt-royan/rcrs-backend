package org.ultra.rcrs.playlistservice.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaylistCommandIntegrationTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void playlistCreated_createsRowInPostgres() throws Exception {
        Thread.sleep(5000);
        UUID id = randomId();

        sendCreatePlaylist(id, "user-1", "Kafka Playlist");

        var playlist = playlistRepository.findById(id).orElseThrow();
        assertThat(playlist.getTitle()).isEqualTo("Kafka Playlist");
        assertThat(playlist.getOwnerId()).isEqualTo("user-1");
        assertThat(playlistTrackRepository.countByPlaylistId(id)).isZero();
    }

    @Test
    @Order(2)
    void tracksAddedToPlaylist_appendsTracks() throws Exception {
        UUID id = randomId();
        sendCreatePlaylist(id, "user-1", "Add Tracks Playlist");

        sendAddTracksToPlaylist(id, List.of("track-1", "track-2"));

        assertThat(playlistTrackRepository.countByPlaylistId(id)).isEqualTo(2);
        assertThat(playlistTrackRepository.findAllByPlaylistId(id))
                .extracting(PlaylistTrack::getTrackId)
                .containsExactlyInAnyOrder("track-1", "track-2");
    }

    @Test
    @Order(3)
    void tracksRemovedFromPlaylist_deletesOnlyRequestedTracks() throws Exception {
        UUID id = randomId();
        sendCreatePlaylist(id, "user-1", "Remove Tracks Playlist");
        sendAddTracksToPlaylist(id, List.of("track-1", "track-2"));

        sendDeleteTracksFromPlaylist(id, List.of("track-1"));

        assertThat(playlistTrackRepository.countByPlaylistId(id)).isEqualTo(1);
        assertThat(playlistTrackRepository.findAllByPlaylistId(id))
                .extracting(PlaylistTrack::getTrackId)
                .containsExactly("track-2");
    }

    @Test
    @Order(4)
    void playlistDeleted_removesPlaylistAndTracks() throws Exception {
        UUID id = randomId();
        sendCreatePlaylist(id, "user-1", "Delete Playlist");
        sendAddTracksToPlaylist(id, List.of("track-1"));

        sendDeletePlaylist(id);

        assertThat(playlistRepository.findById(id)).isEmpty();
        assertThat(playlistTrackRepository.countByPlaylistId(id)).isZero();
    }
}
