package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.model.TrackById;
import org.ultra.rcrs.utils.Base62Utils;

import java.util.List;

@Data
public class TrackDto {

    private String id;

    private Integer trackNumber;

    private AlbumSimplifyDto album;

    private ItemListDto<ArtistSimplifyDto> artists;

    private String title;

    private Long durationMs;

    public TrackDto(TrackById track, List<ArtistSimplifyDto> artists, AlbumSimplifyDto album) {
        this.id = Base62Utils.encode(track.getTrackId());
        this.trackNumber = track.getTrackNumber();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.artists = new ItemListDto<>(artists);
        this.album = album;
    }

}