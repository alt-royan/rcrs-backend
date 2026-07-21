package org.ultra.rcrs.searchservice.dto;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.searchservice.enums.SearchType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchCollectionTest {

    @Test
    void wrapperTypeMapsCorrectly() {
        assertEquals(SearchType.artist, new ArtistResultWrapper(new ArtistSearchResult()).getType());
        assertEquals(SearchType.album, new AlbumResultWrapper(new AlbumSearchResult()).getType());
        assertEquals(SearchType.track, new TrackResultWrapper(new TrackSearchResult()).getType());
    }
}
