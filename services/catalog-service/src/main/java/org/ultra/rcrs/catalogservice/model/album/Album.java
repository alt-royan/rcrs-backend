package org.ultra.rcrs.catalogservice.model.album;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("albums_by_id")
public class Album {

    @PrimaryKey
    @Column("album_id")
    private UUID albumId;

    private String title;

    @Column("total_duration_ms")
    private Long totalDurationMs;

    @Column("album_type")
    private AlbumType albumType;

    @Column("release_date")
    private LocalDate releaseDate;

    @Column("artists")
    private Map<UUID, ArtistRole> artists;

    @Column("image_key")
    private String imageKey;

    @Column("total_tracks")
    private Integer totalTracks;

}
