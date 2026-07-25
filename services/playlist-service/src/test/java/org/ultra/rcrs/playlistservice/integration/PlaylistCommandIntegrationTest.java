package org.ultra.rcrs.playlistservice.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlaylistCommandIntegrationTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void playlistCreated_createsDocumentInMongo() throws Exception {
        Thread.sleep(5000);
        String id = randomId();

        sendCreatePlaylist(id, "user-1", "Kafka Playlist");

        var doc = playlistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("Kafka Playlist");
        assertThat(doc.getOwnerId()).isEqualTo("user-1");
        assertThat(doc.getTrackCount()).isZero();
    }

    @Test
    @Order(2)
    void tracksAddedToPlaylist_appendsTracks() throws Exception {
        String id = randomId();
        sendCreatePlaylist(id, "user-1", "Add Tracks Playlist");

        sendAddTracksToPlaylist(id, List.of("track-1", "track-2"));

        var doc = playlistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTrackCount()).isEqualTo(2);

        var tracks = playlistTrackRepository.findAllByPlaylistIdAndTrackIdIn(id, List.of("track-1", "track-2"))
                .collectList().block();
        assertThat(tracks).hasSize(2);
    }

    @Test
    @Order(3)
    void tracksRemovedFromPlaylist_deletesOnlyRequestedTracks() throws Exception {
        String id = randomId();
        sendCreatePlaylist(id, "user-1", "Remove Tracks Playlist");
        sendAddTracksToPlaylist(id, List.of("track-1", "track-2"));

        sendDeleteTracksFromPlaylist(id, List.of("track-1"));

        var doc = playlistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTrackCount()).isEqualTo(1);

        var remaining = playlistTrackRepository.findAllByPlaylistIdAndTrackIdIn(id, List.of("track-1", "track-2"))
                .collectList().block();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getTrackId()).isEqualTo("track-2");
    }

    @Test
    @Order(4)
    void playlistDeleted_removesPlaylistAndTracks() throws Exception {
        String id = randomId();
        sendCreatePlaylist(id, "user-1", "Delete Playlist");
        sendAddTracksToPlaylist(id, List.of("track-1"));

        sendDeletePlaylist(id);

        assertThat(playlistRepository.findById(id).block()).isNull();
        assertThat(playlistTrackRepository.findAllByPlaylistIdAndTrackIdIn(id, List.of("track-1"))
                .collectList().block()).isEmpty();
    }
}
