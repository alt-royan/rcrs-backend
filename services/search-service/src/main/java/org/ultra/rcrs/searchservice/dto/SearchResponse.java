package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Search results grouped by entity type. Only the sections corresponding to the requested 'type' values are populated; the others are omitted from the JSON response.")
public class SearchResponse {
    @Schema(description = "Paginated artist matches; present only when 'artist' was one of the requested types.")
    private SearchCollection<ArtistResultWrapper> artists;
    @Schema(description = "Paginated album matches; present only when 'album' was one of the requested types.")
    private SearchCollection<AlbumResultWrapper> albums;
    @Schema(description = "Paginated track matches; present only when 'track' was one of the requested types.")
    private SearchCollection<TrackResultWrapper> tracks;
}
