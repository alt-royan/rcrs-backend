package org.ultra.rcrs.metadata.dto;

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
public class AlbumAdminViewDto {

    private String id;
    private LifecycleStatus lifecycleStatus;
    private EntityStatus availabilityStatus;
    private String title;
    private AlbumType type;
    private LocalDateTime releaseDate;
    private Integer year;
    private Integer totalTracks;
    private Integer totalDurationMs;
    private String coverUrl;
    private Boolean explicit;
    private List<ArtistEmbed> artists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ArtistEmbed {
        private String id;
        private String name;
        private String avatarUrl;
        private ArtistRole role;
    }
}
