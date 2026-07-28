package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Full artist detail view returned to admins, including unfiltered availability information.")
public class ArtistAdminViewDto {

    @Schema(description = "Base62-encoded short ID of the artist.")
    private String id;
    @Schema(description = "Artist display name.")
    private String name;
    @Schema(description = "URL of the artist's avatar image.")
    private String avatarUrl;
    @Schema(description = "Social/external links associated with the artist.")
    private List<SocialLinkEmbed> socialLinks;
    @Schema(description = "Free-form tags describing the artist (e.g. genres, moods).")
    private List<String> tags;
    @Schema(description = "Availability/visibility state of the artist (e.g. ACTIVE, HIDDEN, DELETED).")
    private EntityStatus availabilityStatus;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "A single social or external link belonging to an artist.")
    public static class SocialLinkEmbed {
        @Schema(description = "Name of the linked resource/platform (e.g. Instagram, Spotify).")
        private String resourceName;
        @Schema(description = "URL of the linked resource.")
        private String url;
    }
}
