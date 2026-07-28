package org.ultra.rcrs.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.List;
import java.util.Set;

@Schema(description = "A secondary/contributing artist (e.g. featured artist, producer, composer) credited on a track.")
public record OtherArtistDto(
        @Schema(description = "Short (Base62) identifier of an existing artist. Null when the artist is new and identified only by name.")
        String id,
        @Schema(description = "Display name of the artist.")
        String name,
        @Schema(description = "Roles the artist plays on this track (e.g. featured artist, producer, composer).")
        Set<ArtistRole> roles,
        @Schema(description = "Social media links for the artist.")
        List<SocialLinkDto> socialLinks
) {
}
