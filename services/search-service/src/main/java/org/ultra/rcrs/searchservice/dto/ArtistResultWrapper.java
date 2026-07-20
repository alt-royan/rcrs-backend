package org.ultra.rcrs.searchservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
public class ArtistResultWrapper implements ResultWrapper {

    private final SearchType type = SearchType.artist;
    private ArtistSearchResult data;

    public ArtistResultWrapper(ArtistSearchResult data) {
        this.data = data;
    }
}
