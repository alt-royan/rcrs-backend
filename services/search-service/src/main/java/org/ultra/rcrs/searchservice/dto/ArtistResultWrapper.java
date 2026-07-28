package org.ultra.rcrs.searchservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
@Schema(description = "A single artist match, tagged with its entity type.")
public class ArtistResultWrapper implements ResultWrapper {

    @Schema(description = "Always 'artist' for this wrapper.")
    private final SearchType type = SearchType.artist;
    @Schema(description = "The matched artist's details.")
    private ArtistSearchResult data;

    public ArtistResultWrapper(ArtistSearchResult data) {
        this.data = data;
    }
}
