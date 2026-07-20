package org.ultra.rcrs.searchservice.dto;

import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.searchservice.enums.SearchType;

@Getter
@Setter
public class TrackResultWrapper implements ResultWrapper {

    private final SearchType type = SearchType.track;
    private TrackSearchResult data;

    public TrackResultWrapper(TrackSearchResult data) {
        this.data = data;
    }
}
