package org.ultra.rcrs.catalogservice.model.album;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.catalogservice.model.ArtistsOn;
import org.ultra.rcrs.enums.AlbumStatus;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("albums_by_artist")
public class AlbumByArtist {

    @PrimaryKey
    private AlbumByArtistKey key;

    @Column("album_id")
    private UUID albumId;

    @Column("title")
    private String title;

    @Column("total_duration_ms")
    private Integer totalDurationMs;

    @Column("year")
    private Year year;

    @Column("cover_s3_key")
    private String coverS3Key;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("artists")
    private ArtistsOn artists;

    public AlbumByArtist(final Album album, UUID artistId, ArtistRole artistRole) {
        this.key = new AlbumByArtistKey(artistId, album.getKey().getStatus(), artistRole, album.getType(), album.getReleaseDate());
        this.albumId = album.getKey().getId();
        this.title = album.getTitle();
        this.totalDurationMs = album.getTotalDurationMs();
        this.year = album.getYear();
        this.coverS3Key = album.getCoverS3Key();
        this.totalTracks = album.getTotalTracks();
        this.explicit = album.getExplicit();
        this.available = album.getAvailable();
        this.artists = album.getArtists();
    }

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
                name = "album_status",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private AlbumStatus albumStatus;

        @PrimaryKeyColumn(
                name = "artist_role",
                ordinal = 2,
                type = PrimaryKeyType.CLUSTERED
        )
        private ArtistRole artistRole;

        @PrimaryKeyColumn(
                name = "album_type",
                ordinal = 3,
                type = PrimaryKeyType.CLUSTERED
        )
        private AlbumType albumType;

        @PrimaryKeyColumn(
                name = "release_date",
                ordinal = 4,
                type = PrimaryKeyType.CLUSTERED
        )
        private OffsetDateTime releaseDate;

    }

}