package org.ultra.rcrs.catalogservice.model.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("albums_by_id")
public class Album {

    @PrimaryKey
    @Column("album_id")
    private UUID albumId;

    @Column("title")
    private String title;

    @Column("total_duration_ms")
    private Long totalDurationMs;

    @Column("album_type")
    private AlbumType albumType;

    @Column("release_date")
    private LocalDate releaseDate;

    @Column("image_key")
    private String imageKey;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    public Album(AlbumCreateRequest dto) {
        this.title = dto.getTitle();
        this.albumType = dto.getAlbumType();
        this.releaseDate = dto.getReleaseDate();
        this.imageKey = dto.getImageKey();
        this.explicit = dto.getExplicit();
    }
}
