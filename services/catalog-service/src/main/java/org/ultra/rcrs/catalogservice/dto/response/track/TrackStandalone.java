package org.ultra.rcrs.catalogservice.dto.response.track;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumInTrack;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistSimple;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.model.track.TrackByArtist;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDate;
import java.util.Set;

@Data
public class TrackStandalone {

    private String id;

    private EntityStatus status;

    private String title;

    private LocalDate releaseDate;

    private Integer durationMs;

    private Integer trackNumber;

    private Boolean explicit;

    private Boolean available;

    private AlbumInTrack album;

    private Set<ArtistSimple> mainArtists;

    private Set<ArtistSimple> featuredArtists;

    public TrackStandalone(Track track, AlbumInTrack album, Set<ArtistSimple> mainArtists, Set<ArtistSimple> featuredArtists) {
        this.id = Url62.encode(track.getKey().getId());
        this.status = track.getKey().getStatus();
        this.title = track.getTitle();
        this.releaseDate = track.getReleaseDate().toLocalDate();
        this.durationMs = track.getDurationMs();
        this.trackNumber = track.getTrackNumber();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
        this.album = album;
        this.mainArtists = mainArtists;
        this.featuredArtists = featuredArtists;
    }

    public TrackStandalone(TrackByArtist track, AlbumInTrack album, Set<ArtistSimple> mainArtists, Set<ArtistSimple> featuredArtists) {
        this.id = Url62.encode(track.getTrackId());
        this.status = track.getKey().getTrackStatus();
        this.title = track.getTitle();
        this.releaseDate = track.getKey().getReleaseDate().toLocalDate();
        this.durationMs = track.getDurationMs();
        this.trackNumber = track.getTrackNumber();
        this.explicit = track.getExplicit();
        this.available = track.getAvailable();
        this.album = album;
        this.mainArtists = mainArtists;
        this.featuredArtists = featuredArtists;
    }

}