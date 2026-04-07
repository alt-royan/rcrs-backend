package org.ultra.rcrs.catalogservice.dto.response.track;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.model.OthersOnTrack;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class TrackFullDto {

    private String id;

    private EntityStatus status;

    private String title;

    private Instant releaseDate;

    private Integer durationMs;

    private Integer trackNumber;

    private Boolean explicit;

    private Boolean available;

    private AlbumSimpleDto album;

    private List<ArtistOnTrackDto> artists;

    private List<OthersOnTrack.ArtistOther> others;

}