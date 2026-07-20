package org.ultra.rcrs.searchservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
public class AlbumResultWrapper implements ResultWrapper {

    private final SearchType type = SearchType.album;
    private AlbumSearchResult data;

    public AlbumResultWrapper(AlbumSearchResult data) {
        this.data = data;
    }
}
