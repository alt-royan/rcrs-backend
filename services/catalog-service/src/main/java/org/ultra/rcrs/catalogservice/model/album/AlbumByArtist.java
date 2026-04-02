package org.ultra.rcrs.catalogservice.model.album;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("albums_by_artist")
public class AlbumByArtist {

    @PrimaryKey
    private AlbumByArtistKey key;

    @Getter
    @Setter
    @Builder
    @PrimaryKeyClass
    public static class AlbumByArtistKey {

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
                name = "album_id",
                ordinal = 2,
                type = PrimaryKeyType.CLUSTERED
        )
        private UUID albumId;

    }

}