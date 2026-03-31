package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.AlbumById;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.utils.Base62Utils;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class AlbumDto {

    private String id;

    private String title;

    private AlbumType albumType;

    private LocalDate releaseDate;

    private ImageDto coverArt;

    private ItemListDto<ArtistSimplifyDto> artists;

    private ItemListDto<TrackSimplifyDto> tracks;

    private Integer totalTracks;

    public AlbumDto(AlbumById album, Collection<ArtistSimplifyDto> artists, Collection<TrackSimplifyDto> tracks) {
        this.id = Base62Utils.encode(album.getAlbumId());
        this.title = album.getTitle();
        this.albumType = album.getAlbumType();
        this.releaseDate = album.getReleaseDate();
        this.totalTracks = album.getTotalTracks();
        this.coverArt = new ImageDto(S3Utils.createResourceS3Url(album.getImageKey()));
        this.artists = new ItemListDto<>(artists);
        this.tracks = new ItemListDto<>(tracks);
    }

}