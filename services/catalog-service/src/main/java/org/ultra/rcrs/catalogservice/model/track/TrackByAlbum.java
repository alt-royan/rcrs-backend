package org.ultra.rcrs.catalogservice.model.track;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("tracks_by_album")
public class TrackByAlbum {

    @PrimaryKey
    private TrackByAlbumKey key;

    @Column("track_id")
    private UUID trackId;

    private String title;

    @Column("duration_ms")
    private Long durationMs;

    @Column("artists")
    private Map<UUID, ArtistRole> artists;


    @Getter
    @Setter
    @Builder
    @PrimaryKeyClass
    public static class TrackByAlbumKey {

        @PrimaryKeyColumn(
                name = "album_id",
                ordinal = 0,
                type = PrimaryKeyType.PARTITIONED
        )
        private UUID albumId;

        @PrimaryKeyColumn(
                name = "track_number",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private Integer trackNumber;

    }

}
