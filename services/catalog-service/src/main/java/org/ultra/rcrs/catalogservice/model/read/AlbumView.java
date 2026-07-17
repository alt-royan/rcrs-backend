package org.ultra.rcrs.catalogservice.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("album_view")
public class AlbumView {

    private UUID id;

    private LifecycleStatus status;

    private String title;

    private AlbumType type;

    @Column("release_date")
    private LocalDate releaseDate;

    private Integer year;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("total_duration_ms")
    private Integer totalDurationMs;

    @Column("cover_s3_key")
    @JsonProperty("cover_s3_key")
    private String coverS3Key;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    private List<ArtistOnAlbumView> artists;
}
