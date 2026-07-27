package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Schema(description = "Reference to a contributor ('other artist', e.g. producer/writer/engineer) to be associated with a track, distinct from the main/featured performing artists.")
@Data
@NoArgsConstructor
public class OtherArtistDto {

    @Schema(description = "Base62-encoded (Url62) public identifier of the contributor.", example = "1a2B3c4D5e")
    private String id;

    @Schema(description = "Display name of the contributor. Informational only when submitted alongside an id.")
    private String name;

    @Schema(description = "Set of roles the contributor holds on the track. Valid values: MAIN_ARTIST, FEATURED_ARTIST.")
    private Set<ArtistRole> roles;

    @Schema(description = "Social media/website links for the contributor.")
    private List<SocialLinkDto> socialLinks = new ArrayList<>();
}
