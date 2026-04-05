package org.ultra.rcrs.catalogservice.model.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("albums")
public class Album {

    @PrimaryKey
    private AlbumKey key;

    @Column("title")
    private String title;

    @Column("total_duration_ms")
    private Integer totalDurationMs;

    @Column("type")
    private AlbumType type;

    @Column("year")
    private Integer year;

    @Column("release_date")
    private Instant releaseDate;

    @Column("cover_s3_key")
    private String coverS3Key;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("main_artists")
    private Set<UUID> mainArtists;

    @Column("featured_artists")
    private Set<UUID> featuredArtists;

    @Getter
    @Setter
    @AllArgsConstructor
    @PrimaryKeyClass
    public static class AlbumKey {

        @PrimaryKeyColumn(
                name = "id",
                ordinal = 0,
                type = PrimaryKeyType.PARTITIONED
        )
        private UUID id;

        @PrimaryKeyColumn(
                name = "status",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private EntityStatus status;

    }
}
