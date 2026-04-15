package org.ultra.rcrs.searchservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SearchResponse {
    private SearchCollection<ArtistResultWrapper> artists;
    private SearchCollection<AlbumResultWrapper> albums;
    private SearchCollection<TrackResultWrapper> tracks;
}
