package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDate;
import java.util.Collection;

@Data
public class AlbumSimplifyDto {

    private String id;

    private String title;

    private AlbumType albumType;

    private LocalDate releaseDate;

    private String coverUrl;

    private ItemListDto<ArtistSimplifyDto> artists;

    private Integer totalTracks;

    public AlbumSimplifyDto(Album album, Collection<ArtistSimplifyDto> artists) {
        this.id = Url62.encode(album.getAlbumId());
        this.title = album.getTitle();
        this.albumType = album.getAlbumType();
        this.releaseDate = album.getReleaseDate();
        this.totalTracks = album.getTotalTracks();
        this.coverUrl = S3Utils.createResourceS3Url(album.getImageKey());
        this.artists = new ItemListDto<>(artists);
    }

}