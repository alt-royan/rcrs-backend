package org.ultra.rcrs.playlistservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.ultra.rcrs.playlistservice.dto.request.CreatePlaylistRequest;
import org.ultra.rcrs.playlistservice.dto.request.TrackIdsRequest;
import org.ultra.rcrs.playlistservice.model.Playlist;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaylistControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createPlaylist_201CreatedAndPersisted() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTitle("My Mix");
        request.setDescription("chill vibes");
        request.setPrivate(false);

        mockMvc.perform(post("/playlists")
                        .with(jwt().jwt(jwt -> jwt.subject("user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void createPlaylist_missingTitle_400BadRequest() throws Exception {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setDescription("no title");

        mockMvc.perform(post("/playlists")
                        .with(jwt().jwt(jwt -> jwt.subject("user-1")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlaylists_byIds_returnsMatchingPlaylistsWithoutTracks() throws Exception {
        Playlist p1 = createPlaylistDoc("user-1", "Playlist One", false);
        Playlist p2 = createPlaylistDoc("user-1", "Playlist Two", true);
        createPlaylistDoc("user-1", "Playlist Three (excluded)", false);

        mockMvc.perform(get("/playlists")
                        .param("ids", p1.getId(), p2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTracks_pagination_respectsOffsetAndLimit() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Paginated", false);
        for (int i = 0; i < 5; i++) {
            addTrackDoc(playlist.getId(), "track-" + i, i);
        }

        mockMvc.perform(get("/playlists/{id}/tracks", playlist.getId())
                        .param("offset", "2")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void addTracks_thenGetTracks_returnsAppendedTracksInOrder() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Add Tracks", false);
        TrackIdsRequest request = new TrackIdsRequest();
        request.setTrackIds(List.of("track-a", "track-b"));

        mockMvc.perform(put("/playlists/{id}/tracks", playlist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Playlist updated = playlistRepository.findById(playlist.getId()).orElseThrow();
        assertThat(updated.getTrackCount()).isEqualTo(2);

        mockMvc.perform(get("/playlists/{id}/tracks", playlist.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteTracks_removesOnlyRequestedTracks() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Remove Tracks", false);
        addTrackDoc(playlist.getId(), "track-a", 0);
        addTrackDoc(playlist.getId(), "track-b", 1);

        TrackIdsRequest request = new TrackIdsRequest();
        request.setTrackIds(List.of("track-a"));

        mockMvc.perform(delete("/playlists/{id}/tracks", playlist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Playlist updated = playlistRepository.findById(playlist.getId()).orElseThrow();
        assertThat(updated.getTrackCount()).isEqualTo(1);

        mockMvc.perform(get("/playlists/{id}/tracks", playlist.getId())
                        .param("sortBy", "position")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trackId").value("track-b"));
    }

    @Test
    void deleteTracks_reindexesRemainingPositions() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Reindex Tracks", false);
        addTrackDoc(playlist.getId(), "track-a", 0);
        addTrackDoc(playlist.getId(), "track-b", 1);
        addTrackDoc(playlist.getId(), "track-c", 2);

        TrackIdsRequest request = new TrackIdsRequest();
        request.setTrackIds(List.of("track-b"));

        mockMvc.perform(delete("/playlists/{id}/tracks", playlist.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/playlists/{id}/tracks", playlist.getId())
                        .param("sortBy", "position")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].trackId").value("track-a"))
                .andExpect(jsonPath("$[0].position").value(0))
                .andExpect(jsonPath("$[1].trackId").value("track-c"))
                .andExpect(jsonPath("$[1].position").value(1));
    }

    @Test
    void deletePlaylist_removesPlaylistAndItsTracks() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "To Delete", false);
        addTrackDoc(playlist.getId(), "track-a", 0);

        mockMvc.perform(delete("/playlists/{id}", playlist.getId()))
                .andExpect(status().isNoContent());

        assertThat(playlistRepository.findById(playlist.getId())).isEmpty();
    }
}
