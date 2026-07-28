package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Full artist detail view served to the public storefront.")
public class ArtistPublicViewDto {

    @Schema(description = "Base62-encoded short ID of the artist.")
    private String id;
    @Schema(description = "Artist display name.")
    private String name;
    @Schema(description = "URLs of the artist's avatar image, keyed by thumbnail size (SM/MD/LG). Empty if no avatar has been set.")
    private Map<ImageSize, URI> avatar;
    @Schema(description = "Social/external links associated with the artist.")
    private List<SocialLinkEmbed> socialLinks;
    @Schema(description = "Free-form tags describing the artist (e.g. genres, moods).")
    private List<String> tags;
    @Schema(description = "Public availability status of the artist (e.g. ACTIVE); non-public artists are not returned via this API.")
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
