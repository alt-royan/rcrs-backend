package org.ultra.rcrs.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.enums.ArtistRole;

@Schema(description = "A primary artist credited on an album or track being uploaded.")
public record ArtistDto(
        @Schema(description = "Short (Base62) identifier of an existing artist. Null when the artist is new and identified only by name.")
        String id,
        @Schema(description = "Display name of the artist.")
        String name,
        @Schema(description = "Role the artist plays on this album/track (e.g. main artist, producer).")
        ArtistRole role) {
}
