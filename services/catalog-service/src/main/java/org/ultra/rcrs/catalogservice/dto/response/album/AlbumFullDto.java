package org.ultra.rcrs.catalogservice.dto.response.album;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class AlbumFullDto {

    private String id;

    private EntityStatus status;

    private String title;

    private AlbumType type;

    private Instant releaseDate;

    private Integer year;

    private Integer totalTracks;

    private Integer totalDurationMs;

    private String coverUrl;

    private Boolean explicit;

    private Boolean available;

    private List<ArtistOnAlbumDto> artists;

    private List<TrackInAlbumDto> tracks;

}