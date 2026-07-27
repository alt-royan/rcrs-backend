package org.ultra.rcrs.searchservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
@Schema(description = "A single track match, tagged with its entity type.")
public class TrackResultWrapper implements ResultWrapper {

    @Schema(description = "Always 'track' for this wrapper.")
    private final SearchType type = SearchType.track;
    @Schema(description = "The matched track's details.")
    private TrackSearchResult data;

    public TrackResultWrapper(TrackSearchResult data) {
        this.data = data;
    }
}
