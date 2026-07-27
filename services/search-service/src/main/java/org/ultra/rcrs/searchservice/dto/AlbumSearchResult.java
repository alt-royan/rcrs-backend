package org.ultra.rcrs.searchservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "An album matched by search, with its contributing artists and tracks.")
public class AlbumSearchResult {
    @Schema(description = "Short (Base62) public identifier of the album.")
    private String id;
    @Schema(description = "Title of the album.")
    private String title;
    @Schema(description = "Release year of the album.")
    private String year;
    @Schema(description = "URL of the album cover art, if one is set.")
    private String coverUrl;
    @Schema(description = "Public visibility/availability of the album; only meaningful in admin mode, where non-public albums may also be returned.")
    private String availability;
    @Schema(description = "Lifecycle status of the album (e.g. draft/published/archived), primarily relevant in admin mode.")
    private String lifecycleStatus;
    @Schema(description = "Artists credited on the album.")
    private List<NestedArtistDto> artists;
    @Schema(description = "Tracks contained in the album.")
    private List<NestedTrackDto> tracks;
}
