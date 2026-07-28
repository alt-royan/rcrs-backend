package org.ultra.rcrs.searchservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
@Schema(description = "A single album match, tagged with its entity type.")
public class AlbumResultWrapper implements ResultWrapper {

    @Schema(description = "Always 'album' for this wrapper.")
    private final SearchType type = SearchType.album;
    @Schema(description = "The matched album's details.")
    private AlbumSearchResult data;

    public AlbumResultWrapper(AlbumSearchResult data) {
        this.data = data;
    }
}
