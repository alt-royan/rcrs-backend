package org.ultra.rcrs.metadata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Payload for creating a new track within an album.")
@Data
public class TrackUploadRequest {

    @Schema(description = "Base62-encoded (Url62) public identifier of the album this track belongs to.", example = "1a2B3c4D5e")
    private String albumId;

    @Schema(description = "Title of the track.", example = "Come Together")
    private String title;

    @Schema(description = "Position of the track within the album's tracklist, starting at 1.", example = "1")
    private Integer trackNumber;

    @Schema(description = "Whether the track contains explicit content.")
    private Boolean explicit;
}
