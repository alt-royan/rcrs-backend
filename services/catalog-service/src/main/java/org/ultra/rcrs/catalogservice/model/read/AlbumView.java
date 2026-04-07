package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("album_view")
public class AlbumView {

    @Id
    private UUID id;

    private EntityStatus status;

    private String title;

    private AlbumType type;

    @Column("release_date")
    private Instant releaseDate;

    private Integer year;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("total_duration_ms")
    private Integer totalDurationMs;

    @Column("cover_s3_key")
    private String coverS3Key;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    private List<ArtistOnAlbum> artists;
}
