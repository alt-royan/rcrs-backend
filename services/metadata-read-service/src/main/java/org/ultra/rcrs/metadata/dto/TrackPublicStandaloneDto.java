package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Track summary, projected onto an album listing on the public storefront.")
public class TrackPublicStandaloneDto {

    @Schema(description = "Base62-encoded short ID of the track.")
    private String id;
    @Schema(description = "Public availability status of the track (e.g. ACTIVE); non-public tracks are not returned via this API.")
    private EntityStatus availabilityStatus;
    @Schema(description = "Track title.")
    private String title;
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
        @Schema(description = "URL of the album cover artwork.")
        private String coverUrl;
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
        @Schema(description = "URL of the artist's avatar image.")
        private String avatarUrl;
        @Schema(description = "Role of the artist on this track (e.g. PRIMARY, FEATURED, PRODUCER).")
        private ArtistRole role;
    }
}
