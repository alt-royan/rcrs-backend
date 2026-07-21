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
public class ArtistSearchResult {
    private String id;
    private String name;
    private List<String> tags;
    private String availability;
    private List<NestedAlbumDto> albums;
    private List<NestedTrackDto> tracks;
}
