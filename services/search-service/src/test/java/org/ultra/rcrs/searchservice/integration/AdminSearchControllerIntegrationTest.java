package org.ultra.rcrs.searchservice.integration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminSearchControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void searchArtists_includesDeletedArtists() throws Exception {
        indexArtistAdminDoc("a1", "Active Artist", List.of("rock"), "ACTIVE", null, null, null);
        indexArtistAdminDoc("a2", "Deleted Artist", List.of("rock"), "DELETED", null, null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Artist")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.total").value(2));
    }

    @Test
    void searchAlbums_includesDeletedAndDraftAlbums() throws Exception {
        indexAlbumAdminDoc("al1", "Published Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        indexAlbumAdminDoc("al2", "Deleted Album", "2025", "DELETED", "PUBLISHED", null, null);
        indexAlbumAdminDoc("al3", "Draft Album", "2025", "ACTIVE", "CREATED", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Album")
                        .param("type", "album"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.albums.total").value(3));
    }

    @Test
    void searchTracks_includesDeletedAndNonPublishedTracks() throws Exception {
        indexTrackAdminDoc("t1", "Published Track", "ACTIVE", "PUBLISHED", null, null);
        indexTrackAdminDoc("t2", "Deleted Track", "DELETED", "PUBLISHED", null, null);
        indexTrackAdminDoc("t3", "Draft Track", "ACTIVE", "CREATED", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Track")
                        .param("type", "track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks.total").value(3));
    }

    @Test
    void searchAdmin_multipleTypes_returnsAllRequestedTypes() throws Exception {
        indexArtistAdminDoc("a1", "Admin Artist", List.of("rock"), "ACTIVE", null, null, null);
        indexAlbumAdminDoc("al1", "Admin Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        indexTrackAdminDoc("t1", "Admin Track", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Admin")
                        .param("type", "artist,album,track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isNotEmpty())
                .andExpect(jsonPath("$.albums").isNotEmpty())
                .andExpect(jsonPath("$.tracks").isNotEmpty());
    }

    @Test
    void searchAdmin_singleType_onlyReturnsRequestedType() throws Exception {
        indexArtistAdminDoc("a1", "Only Artist", List.of("rock"), "ACTIVE", null, null, null);
        indexAlbumAdminDoc("al1", "Only Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Only")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isNotEmpty())
                .andExpect(jsonPath("$.albums").doesNotExist())
                .andExpect(jsonPath("$.tracks").doesNotExist());
    }

    @Test
    void searchAdmin_noResults_returnsEmpty() throws Exception {
        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "nonexistent")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.total").value(0));
    }

    @Test
    void searchAdmin_pagination() throws Exception {
        for (int i = 0; i < 5; i++) {
            indexArtistAdminDoc("a" + i, "Page Artist " + i, List.of("rock"), "ACTIVE", null, null, null);
        }
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Page")
                        .param("type", "artist")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.size").value(2))
                .andExpect(jsonPath("$.artists.items.length()").value(2));
    }

    @Test
    void searchAdmin_artistsNestedAlbumsAndTracksIncluded() throws Exception {
        indexArtistAdminDoc("a1", "Nested Admin", List.of("rock"), "ACTIVE", null,
                List.of(nestedAlbum("al1", "Admin Album")),
                List.of(nested("t1", "Admin Track")));
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Nested")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.items[0].data.albums[0].id").value("al1"))
                .andExpect(jsonPath("$.artists.items[0].data.tracks[0].id").value("t1"));
    }

    @Test
    void searchAdmin_albumNestedArtistsAndTracksIncluded() throws Exception {
        indexAlbumAdminDoc("al1", "Nested Album Admin", "2025", "ACTIVE", "PUBLISHED",
                List.of(nested("a1", "Admin Artist")),
                List.of(nested("t1", "Admin Track")));
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Nested")
                        .param("type", "album"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.albums.items[0].data.artists[0].id").value("a1"))
                .andExpect(jsonPath("$.albums.items[0].data.tracks[0].id").value("t1"));
    }

    @Test
    void searchAdmin_trackNestedArtistsAndAlbumIncluded() throws Exception {
        indexTrackAdminDoc("t1", "Nested Track Admin", "ACTIVE", "PUBLISHED",
                List.of(nested("a1", "Admin Artist")),
                nestedAlbum("al1", "Admin Album"));
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("admin", "true")
                        .param("q", "Nested")
                        .param("type", "track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks.items[0].data.artists[0].id").value("a1"))
                .andExpect(jsonPath("$.tracks.items[0].data.album.id").value("al1"));
    }
}
