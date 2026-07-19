package org.ultra.rcrs.metadata.dto;

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
public class TrackPublicStandaloneDto {

    private String id;
    private EntityStatus availabilityStatus;
    private String title;
    private Integer durationMs;
    private Integer trackNumber;
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
