package org.ultra.rcrs.catalogservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "tracks")
public class TrackDocument {

    @Id
    private String id;
    private String status;
    private String title;
    private String releaseDate;
    private Integer durationMs;
    private Integer trackNumber;
    private Boolean explicit;
    private Boolean available;
    private AlbumEmbed album;
    private List<ArtistEmbed> artists;
    private List<OtherArtistEmbed> others;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlbumEmbed {
        private String id;
        private String title;
        private String coverUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ArtistEmbed {
        private String id;
        private String name;
        private String avatarUrl;
        private String role;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OtherArtistEmbed {
        private String name;
        private List<String> roles;
        private List<ArtistDocument.SocialLinkEmbed> socialLinks;
    }
}
