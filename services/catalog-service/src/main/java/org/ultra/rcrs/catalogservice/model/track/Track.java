package org.ultra.rcrs.catalogservice.model.track;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.catalogservice.model.ArtistOther;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("tracks_by_id")
public class Track {

    @PrimaryKey
    private TrackKey key;

    @Column("title")
    private String title;

    @Column("release_date")
    private OffsetDateTime releaseDate;

    @Column("duration_ms")
    private Integer durationMs;

    @Column("track_number")
    private Integer trackNumber;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("album_id")
    private UUID albumId;

    @Column("main_artists")
    private Set<UUID> mainArtists;

    @Column("featured_artists")
    private Set<UUID> featuredArtists;

    @Column("other")
    private List<ArtistOther> others;

    @Getter
    @Setter
    @AllArgsConstructor
    @PrimaryKeyClass
    public static class TrackKey {

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