package org.ultra.rcrs.searchservice.integration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicSearchControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void searchArtists_returnsMatchingResults() throws Exception {
        indexArtistPublicDoc("a1", "The Rolling Stones", List.of("rock"), "ACTIVE", null, null);
        indexArtistPublicDoc("a2", "The Beatles", List.of("rock", "pop"), "ACTIVE", null, null);
        indexArtistPublicDoc("a3", "Miles Davis", List.of("jazz"), "ACTIVE", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "the")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isNotEmpty())
                .andExpect(jsonPath("$.artists.total").value(2))
                .andExpect(jsonPath("$.artists.items[0].data.name").isNotEmpty());
    }

    @Test
    void searchAlbums_returnsMatchingResults() throws Exception {
        indexAlbumPublicDoc("al1", "Abbey Road", "1969", "ACTIVE",
                List.of(nested("a1", "The Beatles")), null);
        indexAlbumPublicDoc("al2", "Let It Be", "1970", "ACTIVE",
                List.of(nested("a1", "The Beatles")), null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Abbey")
                        .param("type", "album"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.albums").isNotEmpty())
                .andExpect(jsonPath("$.albums.total").value(1))
                .andExpect(jsonPath("$.albums.items[0].data.title").value("Abbey Road"));
    }

    @Test
    void searchTracks_returnsMatchingResults() throws Exception {
        indexTrackPublicDoc("t1", "Come Together", "ACTIVE",
                List.of(nested("a1", "The Beatles")), nestedAlbum("al1", "Abbey Road"));
        indexTrackPublicDoc("t2", "Let It Be", "ACTIVE",
                List.of(nested("a1", "The Beatles")), nestedAlbum("al2", "Let It Be"));
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Come Together")
                        .param("type", "track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tracks").isNotEmpty())
                .andExpect(jsonPath("$.tracks.total").value(1))
                .andExpect(jsonPath("$.tracks.items[0].data.title").value("Come Together"));
    }

    @Test
    void searchMultipleTypes_returnsAllRequestedTypes() throws Exception {
        indexArtistPublicDoc("a1", "Test Artist", List.of("rock"), "ACTIVE", null, null);
        indexAlbumPublicDoc("al1", "Test Album", "2025", "ACTIVE", null, null);
        indexTrackPublicDoc("t1", "Test Track", "ACTIVE", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Test")
                        .param("type", "artist,album,track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isNotEmpty())
                .andExpect(jsonPath("$.albums").isNotEmpty())
                .andExpect(jsonPath("$.tracks").isNotEmpty());
    }

    @Test
    void searchSingleType_onlyReturnsRequestedType() throws Exception {
        indexArtistPublicDoc("a1", "Solo Artist", List.of("rock"), "ACTIVE", null, null);
        indexAlbumPublicDoc("al1", "Solo Album", "2025", "ACTIVE", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Solo")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists").isNotEmpty())
                .andExpect(jsonPath("$.albums").doesNotExist())
                .andExpect(jsonPath("$.tracks").doesNotExist());
    }

    @Test
    void search_noResults_returnsEmptyCollections() throws Exception {
        mockMvc.perform(get("/search")
                        .param("q", "nonexistent")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.total").value(0));
    }

    @Test
    void search_pagination_pageAndSize() throws Exception {
        for (int i = 0; i < 5; i++) {
            indexArtistPublicDoc("a" + i, "Paginated Artist " + i, List.of("rock"), "ACTIVE", null, null);
        }
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Paginated")
                        .param("type", "artist")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.size").value(2))
                .andExpect(jsonPath("$.artists.items.length()").value(2));
    }

    @Test
    void search_queryParamRequired_returns400WhenMissing() throws Exception {
        mockMvc.perform(get("/search")
                        .param("type", "artist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_typeParamRequired_returns400WhenMissing() throws Exception {
        mockMvc.perform(get("/search")
                        .param("q", "test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_emptyQuery_returnsResults() throws Exception {
        indexArtistPublicDoc("a1", "Some Artist", List.of("rock"), "ACTIVE", null, null);
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "")
                        .param("type", "artist"))
                .andExpect(status().isOk());
    }

    @Test
    void search_artistsNestedAlbumsAndTracksIncluded() throws Exception {
        indexArtistPublicDoc("a1", "Nested Artist", List.of("rock"), "ACTIVE",
                List.of(nestedAlbum("al1", "Some Album")),
                List.of(nested("t1", "Some Track")));
        refreshAllIndices();

        mockMvc.perform(get("/search")
                        .param("q", "Nested")
                        .param("type", "artist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.items[0].data.albums[0].id").value("al1"))
                .andExpect(jsonPath("$.artists.items[0].data.tracks[0].id").value("t1"));
    }
}
