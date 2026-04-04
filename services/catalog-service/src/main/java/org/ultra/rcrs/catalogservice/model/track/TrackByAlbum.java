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

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("tracks_by_album")
public class TrackByAlbum {

    @PrimaryKey
    private TrackByAlbumKey key;

    @Column("track_id")
    private UUID trackId;

    @Column("title")
    private String title;

    @Column("duration_ms")
    private Integer durationMs;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("artists")
    private ArtistsOn artists;

    public TrackByAlbum(final Track track) {
        this.key = new TrackByAlbumKey(track.getAlbumId(), track.getKey().getStatus(), track.getTrackNumber());
        this.trackId = track.getKey().getId();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
        this.artists = track.getArtists();
    }

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
                name = "track_status",
                ordinal = 1,
                type = PrimaryKeyType.CLUSTERED
        )
        private TrackStatus trackStatus;

        @PrimaryKeyColumn(
                name = "track_number",
                ordinal = 2,
                type = PrimaryKeyType.CLUSTERED
        )
        private Integer trackNumber;

    }

}
