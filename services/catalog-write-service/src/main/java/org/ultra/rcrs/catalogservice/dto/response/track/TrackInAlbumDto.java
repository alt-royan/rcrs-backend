package org.ultra.rcrs.catalogservice.dto.response.track;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.List;

@Data
@Builder
public class TrackInAlbumDto {

    private String id;
    private LifecycleStatus status;
    private String title;
    private Integer durationMs;
    private Integer trackNumber;
    private Boolean explicit;
    private Boolean available;
    private List<ArtistOnTrackDto> artists;
}
