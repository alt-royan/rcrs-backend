package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Full album detail view returned to admins, including unfiltered lifecycle information.")
public class AlbumAdminViewDto {

    @Schema(description = "Base62-encoded short ID of the album.")
    private String id;
    @Schema(description = "Editorial lifecycle state of the album (e.g. DRAFT, PUBLISHED, ARCHIVED).")
    private LifecycleStatus lifecycleStatus;
    @Schema(description = "Availability/visibility state of the album (e.g. ACTIVE, HIDDEN, DELETED).")
    private EntityStatus availabilityStatus;
    @Schema(description = "Album title.")
    private String title;
    @Schema(description = "Album type (e.g. ALBUM, SINGLE, EP, COMPILATION).")
    private AlbumType type;
    @Schema(description = "Official release date/time of the album.")
    private LocalDateTime releaseDate;
    @Schema(description = "Release year, derived from the release date.")
    private Integer year;
    @Schema(description = "Total number of tracks on the album.")
    private Integer totalTracks;
    @Schema(description = "Total playback duration of the album, in milliseconds.")
    private Integer totalDurationMs;
    @Schema(description = "URL of the album cover artwork.")
    private String coverUrl;
    @Schema(description = "Whether the album contains explicit content.")
    private Boolean explicit;
    @Schema(description = "Artists credited on the album.")
    private List<ArtistEmbed> artists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "Minimal embedded reference to an artist credited on the album.")
    public static class ArtistEmbed {
        @Schema(description = "Base62-encoded short ID of the artist.")
        private String id;
        @Schema(description = "Artist display name.")
        private String name;
        @Schema(description = "URL of the artist's avatar image.")
        private String avatarUrl;
        @Schema(description = "Role of the artist on this album (e.g. PRIMARY, FEATURED, PRODUCER).")
        private ArtistRole role;
    }
}
