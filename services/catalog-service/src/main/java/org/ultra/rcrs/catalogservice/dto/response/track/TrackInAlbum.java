package org.ultra.rcrs.catalogservice.dto.response.track;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistSimple;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;

import java.util.Set;

@Data
public class TrackInAlbum {

    private String id;

    private EntityStatus status;

    private Integer trackNumber;

    private String title;

    private Integer durationMs;

    private Boolean explicit;

    private Boolean available;

    private Set<ArtistSimple> mainArtists;

    private Set<ArtistSimple> featuredArtists;

    public TrackInAlbum(TrackByAlbum track, Set<ArtistSimple> mainArtists, Set<ArtistSimple> featuredArtists) {
        this.id = Url62.encode(track.getTrackId());
        this.status = track.getKey().getTrackStatus();
        this.trackNumber = track.getKey().getTrackNumber();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
        this.mainArtists = mainArtists;
        this.featuredArtists = featuredArtists;
    }

}