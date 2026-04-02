package org.ultra.rcrs.catalogservice.model.track;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.catalogservice.dto.request.TrackCreateDto;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;

import java.util.Set;
import java.util.UUID;

import static org.springframework.data.cassandra.core.mapping.Embedded.OnEmpty.USE_NULL;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("tracks_by_id")
public class Track {

    @PrimaryKey
    @Column("track_id")
    private UUID trackId;

    private String title;

    @Column("duration_ms")
    private Long durationMs;

    @Column("album_id")
    private UUID albumId;

    @Column("artists")
    private Set<ArtistWithRole> artists;

    @Column("track_number")
    private Integer trackNumber;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    public Track(TrackCreateDto dto) {
        this.title = dto.getTitle();
        this.albumId = dto.getAlbumId();
        this.artists = dto.getArtists();
        this.trackNumber = dto.getTrackNumber();
        this.explicit = dto.getExplicit();
    }

}