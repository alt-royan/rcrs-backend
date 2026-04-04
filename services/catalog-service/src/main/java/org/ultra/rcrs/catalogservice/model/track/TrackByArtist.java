package org.ultra.rcrs.catalogservice.model.track;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.catalogservice.model.ArtistsOn;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.TrackStatus;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("tracks_by_artist")
public class TrackByArtist {

    @PrimaryKey
    private TrackByArtistKey key;

    @Column("track_id")
    private UUID trackId;

    @Column("title")
    private String title;

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

    @Column("artists")
    private ArtistsOn artists;

    public TrackByArtist(final Track track, UUID artistId, ArtistRole artistRole) {
        this.key = new TrackByArtistKey(artistId, track.getKey().getStatus(), artistRole, track.getReleaseDate());
        this.trackId = track.getKey().getId();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.trackNumber = track.getTrackNumber();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
        this.albumId = track.getAlbumId();
        this.artists = track.getArtists();
    }


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
                name = "track_status",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private TrackStatus trackStatus;

        @PrimaryKeyColumn(
                name = "artist_role",
                ordinal = 2,
                type = PrimaryKeyType.CLUSTERED
        )
        private ArtistRole artistRole;

        @PrimaryKeyColumn(
                name = "release_date",
                ordinal = 3,
                type = PrimaryKeyType.CLUSTERED
        )
        private OffsetDateTime releaseDate;

    }

}
