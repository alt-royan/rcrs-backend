package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Full track detail view served to the public storefront.")
public class TrackPublicViewDto {

    @Schema(description = "Base62-encoded short ID of the track.")
    private String id;
    @Schema(description = "Public availability status of the track (e.g. ACTIVE); non-public tracks are not returned via this API.")
    private EntityStatus availabilityStatus;
    @Schema(description = "Track title.")
    private String title;
    @Schema(description = "Official release date/time of the track.")
    private LocalDateTime releaseDate;
    @Schema(description = "Playback duration of the track, in milliseconds.")
    private Integer durationMs;
    @Schema(description = "Position of the track within its album's tracklist.")
    private Integer trackNumber;
    @Schema(description = "Whether the track contains explicit content.")
    private Boolean explicit;
    @Schema(description = "Album this track belongs to.")
    private AlbumEmbed album;
    @Schema(description = "Primary/credited artists performing the track.")
    private List<ArtistEmbed> artists;
    @Schema(description = "Other contributing artists (e.g. producers, songwriters) not listed as primary credits.")
    private List<OtherArtistEmbed> others;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "Minimal embedded reference to the album a track belongs to.")
    public static class AlbumEmbed {
        @Schema(description = "Base62-encoded short ID of the album.")
        private String id;
        @Schema(description = "Album title.")
        private String title;
        @Schema(description = "URLs of the album cover artwork, keyed by thumbnail size (SM/MD/LG). Empty if no cover has been set.")
        private Map<ImageSize, URI> cover;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "Minimal embedded reference to an artist credited on the track.")
    public static class ArtistEmbed {
        @Schema(description = "Base62-encoded short ID of the artist.")
        private String id;
        @Schema(description = "Artist display name.")
        private String name;
        @Schema(description = "URLs of the artist's avatar image, keyed by thumbnail size (SM/MD/LG). Empty if no avatar has been set.")
        private Map<ImageSize, URI> avatar;
        @Schema(description = "Role of the artist on this track (e.g. PRIMARY, FEATURED, PRODUCER).")
        private ArtistRole role;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "Embedded reference to a contributing artist not part of the primary credit list.")
    public static class OtherArtistEmbed {
        @Schema(description = "Base62-encoded short ID of the artist.")
        private String id;
        @Schema(description = "Artist display name.")
        private String name;
        @Schema(description = "Roles this artist held in producing the track (e.g. PRODUCER, SONGWRITER).")
        private List<ArtistRole> roles;
        @Schema(description = "Social/external links associated with the artist.")
        private List<SocialLinkEmbed> socialLinks;
    }

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
