package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlbumSearchResult {
    private String id;
    private String title;
    private String year;
    private String availability;
    private String lifecycleStatus;
    private List<NestedArtistDto> artists;
    private List<NestedTrackDto> tracks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NestedArtistDto {
        private String id;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NestedTrackDto {
        private String id;
        private String title;
    }
}
