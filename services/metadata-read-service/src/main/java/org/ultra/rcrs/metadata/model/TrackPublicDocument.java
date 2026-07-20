package org.ultra.rcrs.metadata.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tracks")
public class TrackPublicDocument {

    @Id
    private String id;
    private LifecycleStatus lifecycleStatus;
    private EntityStatus availabilityStatus;
    private String title;
    private LocalDateTime releaseDate;
    private Integer durationMs;
    private Integer trackNumber;
    private Boolean explicit;
    private AlbumEmbed album;
    private List<ArtistEmbed> artists;
    private List<OtherArtistEmbed> others;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AlbumEmbed {
        private String id;
        private String title;
        private String coverS3Key;
    }

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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OtherArtistEmbed {
        private String id;
        private String name;
        private List<ArtistRole> roles;
        private List<ArtistPublicDocument.SocialLinkEmbed> socialLinks;
    }
}
