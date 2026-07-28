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
@Schema(description = "A track matched by search, with its contributing artists and parent album.")
public class TrackSearchResult {
    @Schema(description = "Short (Base62) public identifier of the track.")
    private String id;
    @Schema(description = "Title of the track.")
    private String title;
    @Schema(description = "Public visibility/availability of the track; only meaningful in admin mode, where non-public tracks may also be returned.")
    private String availability;
    @Schema(description = "Lifecycle status of the track (e.g. draft/published/archived), primarily relevant in admin mode.")
    private String lifecycleStatus;
    @Schema(description = "Artists credited on the track.")
    private List<NestedArtistDto> artists;
    @Schema(description = "The album the track belongs to.")
    private NestedAlbumDto album;
}
