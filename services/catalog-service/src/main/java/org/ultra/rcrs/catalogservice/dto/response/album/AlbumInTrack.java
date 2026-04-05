package org.ultra.rcrs.catalogservice.dto.response.album;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistSimple;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDate;
import java.time.Year;
import java.util.Set;

@Data
public class AlbumInTrack {

    private String id;

    private EntityStatus status;

    private String title;

    private Integer totalDurationMs;

    private AlbumType type;

    private Year year;

    private LocalDate releaseDate;

    private String coverUrl;

    private Integer totalTracks;

    private Boolean explicit;

    private Boolean available;

    private Set<ArtistSimple> mainArtists;

    public AlbumInTrack(Album album, String coverUrl, Set<ArtistSimple> mainArtists) {
        this.id = Url62.encode(album.getKey().getId());
        this.status = album.getKey().getStatus();
        this.title = album.getTitle();
        this.totalDurationMs = album.getTotalDurationMs();
        this.type = album.getType();
        this.year = album.getYear();
        this.releaseDate = album.getReleaseDate().toLocalDate();
        this.coverUrl = coverUrl;
        this.totalTracks = album.getTotalTracks();
        this.explicit = album.getExplicit();
        this.available = album.getAvailable();
        this.mainArtists = mainArtists;
    }

}