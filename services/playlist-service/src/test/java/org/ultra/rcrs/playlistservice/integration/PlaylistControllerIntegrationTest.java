package org.ultra.rcrs.playlistservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.ultra.rcrs.playlistservice.dto.request.CreatePlaylistRequest;
import org.ultra.rcrs.playlistservice.dto.request.IdsRequest;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.utils.Url62;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        assertThat(playlistRepository.findAll())
                .extracting(Playlist::getTitle)
                .containsExactly("My Mix");
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
    void getPlaylists_byIds_returnsOnlyRequestedPlaylists() throws Exception {
        Playlist p1 = createPlaylistDoc("user-1", "Playlist One", false);
        Playlist p2 = createPlaylistDoc("user-1", "Playlist Two", true);
        createPlaylistDoc("user-1", "Playlist Three (excluded)", false);

        IdsRequest request = new IdsRequest(List.of(Url62.encode(p1.getId()), Url62.encode(p2.getId())));

        mockMvc.perform(post("/playlists/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPlaylist_byId_returnsViewWithTrackCount() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Single Playlist", false);
        addTrackDoc(playlist.getId(), "track-a", 1);
        addTrackDoc(playlist.getId(), "track-b", 2);

        mockMvc.perform(get("/playlists/{id}", Url62.encode(playlist.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Single Playlist"))
                .andExpect(jsonPath("$.ownerId").value("user-1"))
                .andExpect(jsonPath("$.trackCount").value(2));
    }

    @Test
    void getTracks_pagination_respectsOffsetAndLimit() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Paginated", false);
        for (int i = 1; i <= 5; i++) {
            addTrackDoc(playlist.getId(), "track-" + i, i);
        }

        mockMvc.perform(get("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .param("offset", "2")
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.offset").value(2))
                .andExpect(jsonPath("$.limit").value(2));
    }

    @Test
    void addTracks_thenGetTracks_returnsAppendedTracksInOrder() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Add Tracks", false);
        IdsRequest request = new IdsRequest(List.of("track-a", "track-b"));

        mockMvc.perform(put("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(playlistTrackRepository.countByPlaylistId(playlist.getId())).isEqualTo(2);

        mockMvc.perform(get("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .param("sortBy", "position")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].trackId").value("track-a"))
                .andExpect(jsonPath("$.items[1].trackId").value("track-b"));
    }

    @Test
    void deleteTracks_removesOnlyRequestedTracks() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Remove Tracks", false);
        addTrackDoc(playlist.getId(), "track-a", 1);
        addTrackDoc(playlist.getId(), "track-b", 2);

        IdsRequest request = new IdsRequest(List.of("track-a"));

        mockMvc.perform(delete("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(playlistTrackRepository.countByPlaylistId(playlist.getId())).isEqualTo(1);

        mockMvc.perform(get("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .param("sortBy", "position")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].trackId").value("track-b"));
    }

    @Test
    void deleteTracks_reindexesRemainingPositions() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "Reindex Tracks", false);
        addTrackDoc(playlist.getId(), "track-a", 1);
        addTrackDoc(playlist.getId(), "track-b", 2);
        addTrackDoc(playlist.getId(), "track-c", 3);

        IdsRequest request = new IdsRequest(List.of("track-b"));

        mockMvc.perform(delete("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/playlists/{id}/tracks", Url62.encode(playlist.getId()))
                        .param("sortBy", "position")
                        .param("direction", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].trackId").value("track-a"))
                .andExpect(jsonPath("$.items[0].position").value(1))
                .andExpect(jsonPath("$.items[1].trackId").value("track-c"))
                .andExpect(jsonPath("$.items[1].position").value(2));
    }

    @Test
    void deletePlaylist_removesPlaylistAndItsTracks() throws Exception {
        Playlist playlist = createPlaylistDoc("user-1", "To Delete", false);
        addTrackDoc(playlist.getId(), "track-a", 1);

        mockMvc.perform(delete("/playlists/{id}", Url62.encode(playlist.getId())))
                .andExpect(status().isNoContent());

        assertThat(playlistRepository.findById(playlist.getId())).isEmpty();
        assertThat(playlistTrackRepository.countByPlaylistId(playlist.getId())).isZero();
    }
}
