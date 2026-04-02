package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.utils.Url62;

import java.util.List;

@Data
public class TrackSimplifyDto {

    private String id;

    private Integer trackNumber;

    private ItemListDto<ArtistSimplifyDto> artists;

    private String title;

    private Long durationMs;

    public TrackSimplifyDto(TrackByAlbum track, List<ArtistSimplifyDto> artists) {
        this.id = Url62.encode(track.getTrackId());
        this.trackNumber = track.getKey().getTrackNumber();
        this.title = track.getTitle();
        this.durationMs = track.getDurationMs();
        this.artists = new ItemListDto<>(artists);
    }

}