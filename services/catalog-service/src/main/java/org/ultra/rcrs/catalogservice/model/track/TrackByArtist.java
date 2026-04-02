package org.ultra.rcrs.catalogservice.model.track;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@Table("tracks_by_artist")
public class TrackByArtist {

    @PrimaryKey
    private TrackByArtistKey key;

    @Getter
    @Setter
    @Builder
    @PrimaryKeyClass
    public static class TrackByArtistKey {

        @PrimaryKeyColumn(
                name = "artist_id",
                ordinal = 0,
                type = PrimaryKeyType.PARTITIONED
        )
        private UUID artistId;

        @PrimaryKeyColumn(
                name = "artist_role",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private ArtistRole artistRole;

        @PrimaryKeyColumn(
                name = "track_id",
                ordinal = 2,
                type = PrimaryKeyType.CLUSTERED
        )
        private UUID trackId;

    }

}
