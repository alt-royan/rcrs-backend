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
@Document(collection = "albums")
public class AlbumDocument {

    @Id
    private String id;
    private String status;
    private String title;
    private String type;
    private String releaseDate;
    private Integer year;
    private Integer totalTracks;
    private Integer totalDurationMs;
    private String coverUrl;
    private Boolean explicit;
    private Boolean available;
    private List<ArtistEmbed> artists;
    private List<TrackEmbed> tracks;

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
    public static class TrackEmbed {
        private String id;
        private String status;
        private String title;
        private Integer durationMs;
        private Integer trackNumber;
        private Boolean explicit;
        private Boolean available;
        private List<TrackDocument.ArtistEmbed> artists;
    }
}
