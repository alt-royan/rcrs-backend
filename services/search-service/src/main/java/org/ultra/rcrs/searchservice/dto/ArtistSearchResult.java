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
@Schema(description = "An artist matched by search, with its associated albums and tracks.")
public class ArtistSearchResult {
    @Schema(description = "Short (Base62) public identifier of the artist.")
    private String id;
    @Schema(description = "Display name of the artist.")
    private String name;
    @Schema(description = "URL of the artist's avatar/profile image, if one is set.")
    private String avatarUrl;
    @Schema(description = "Genre/style tags associated with the artist.")
    private List<String> tags;
    @Schema(description = "Public visibility/availability of the artist (e.g. whether it is publicly listed); only meaningful in admin mode, where non-public artists may also be returned.")
    private String availability;
    @Schema(description = "Albums the artist appears on.")
    private List<NestedAlbumDto> albums;
    @Schema(description = "Tracks the artist appears on.")
    private List<NestedTrackDto> tracks;
}
