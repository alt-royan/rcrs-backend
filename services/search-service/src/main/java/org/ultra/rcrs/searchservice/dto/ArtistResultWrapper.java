package org.ultra.rcrs.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
@AllArgsConstructor
public class ArtistResultWrapper implements ResultWrapper {

    private final SearchType type = SearchType.artist;
    private Object data;
}
