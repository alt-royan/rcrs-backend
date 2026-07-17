package org.ultra.rcrs.catalogservice.dto.response.album;

import lombok.Builder;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AlbumOfArtistDto {
    private String id;
    private ArtistRole artistRole;
    private LifecycleStatus status;
    private String title;
    private AlbumType type;
    private LocalDate releaseDate;
    private Integer year;
    private Integer totalTracks;
    private Integer totalDurationMs;
    private String coverUrl;
    private Boolean explicit;
    private Boolean available;
    private List<ArtistOnAlbumDto> artists;
}
