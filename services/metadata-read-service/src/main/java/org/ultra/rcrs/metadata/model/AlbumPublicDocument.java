package org.ultra.rcrs.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
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
@Document(collection = "albums")
public class AlbumPublicDocument {

    @Id
    private String id;
    private LifecycleStatus lifecycleStatus;
    private EntityStatus availabilityStatus;
    private String title;
    private AlbumType type;
    private LocalDateTime releaseDate;
    private Integer year;
    private Integer totalTracks;
    private Integer totalDurationMs;
    private String coverS3Key;
    private Boolean explicit;
    private List<ArtistEmbed> artists;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ArtistEmbed {
        private String id;
        private String name;
        private String avatarS3Key;
        private ArtistRole role;
    }
}
