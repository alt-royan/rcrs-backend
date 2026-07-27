package org.ultra.rcrs.metadata.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Full album detail view served to the public storefront.")
public class AlbumPublicViewDto {

    @Schema(description = "Base62-encoded short ID of the album.")
    private String id;
    @Schema(description = "Public availability status of the album (e.g. ACTIVE); non-public albums are not returned via this API.")
    private EntityStatus availabilityStatus;
    @Schema(description = "Album title.")
    private String title;
    @Schema(description = "Album type (e.g. ALBUM, SINGLE, EP, COMPILATION).")
    private AlbumType type;
    @Schema(description = "Calendar release date of the album, ISO-8601 (yyyy-MM-dd).")
    private LocalDate releaseDate;
    @Schema(description = "Total number of tracks on the album.")
    private Integer totalTracks;
    @Schema(description = "Total playback duration of the album, in milliseconds.")
    private Integer totalDurationMs;
    @Schema(description = "URLs of the album cover artwork, keyed by thumbnail size (SM/MD/LG). Empty if no cover has been set.")
    private Map<ImageSize, URI> cover;
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
        @Schema(description = "URLs of the artist's avatar image, keyed by thumbnail size (SM/MD/LG). Empty if no avatar has been set.")
        private Map<ImageSize, URI> avatar;
        @Schema(description = "Role of the artist on this album (e.g. PRIMARY, FEATURED, PRODUCER).")
        private ArtistRole role;
    }
}
