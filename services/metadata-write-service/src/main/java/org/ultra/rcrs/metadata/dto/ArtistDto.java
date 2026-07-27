package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.ultra.rcrs.enums.ArtistRole;

@Schema(description = "Reference to an artist to be associated with an album or track, along with the role that artist plays on that entity.")
@Data
public class ArtistDto {

    @Schema(description = "Base62-encoded (Url62) public identifier of an existing artist.", example = "1a2B3c4D5e")
    private String id;

    @Schema(description = "Display name of the artist. Informational only when submitted alongside an id; the persisted name comes from the existing artist record.")
    private String name;

    @Schema(description = "Role the artist plays on the album/track. Valid values: MAIN_ARTIST, FEATURED_ARTIST. Defaults to MAIN_ARTIST.")
    private ArtistRole role = ArtistRole.MAIN_ARTIST;
}
