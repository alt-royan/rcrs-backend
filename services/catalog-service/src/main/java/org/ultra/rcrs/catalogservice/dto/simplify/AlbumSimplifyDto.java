package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ImageDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.model.AlbumByArtist;
import org.ultra.rcrs.catalogservice.model.AlbumByTrack;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.utils.Base62Utils;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class AlbumSimplifyDto {

    private String id;

    private String title;

    private AlbumType albumType;

    private LocalDate releaseDate;

    private ImageDto coverArt;

    private ItemListDto<ArtistSimplifyDto> artists;

    private Integer totalTracks;

    public AlbumSimplifyDto(AlbumByTrack album, Collection<ArtistSimplifyDto> artists) {
        this.id = Base62Utils.encode(album.getKey().getAlbumId());
        this.title = album.getTitle();
        this.albumType = album.getAlbumType();
        this.releaseDate = album.getReleaseDate();
        this.totalTracks = album.getTotalTracks();
        this.coverArt = new ImageDto(album.getImageUrl());
        this.artists = new ItemListDto<>(artists);
    }

    public AlbumSimplifyDto(AlbumByArtist album, Collection<ArtistSimplifyDto> artists) {
        this.id = Base62Utils.encode(album.getKey().getAlbumId());
        this.title = album.getTitle();
        this.albumType = album.getAlbumType();
        this.releaseDate = album.getKey().getReleaseDate();
        this.totalTracks = album.getTotalTracks();
        this.coverArt = new ImageDto(album.getImageUrl());
        this.artists = new ItemListDto<>(artists);
    }

}