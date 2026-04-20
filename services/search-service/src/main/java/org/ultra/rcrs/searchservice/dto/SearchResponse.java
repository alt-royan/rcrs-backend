package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {
    private SearchCollection<ArtistResultWrapper> artists;
    private SearchCollection<AlbumResultWrapper> albums;
    private SearchCollection<TrackResultWrapper> tracks;
}
