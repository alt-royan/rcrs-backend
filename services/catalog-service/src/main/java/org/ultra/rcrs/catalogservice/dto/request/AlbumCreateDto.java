package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class AlbumCreateDto {

    @NotNull
    private String title;

    @NotNull
    private AlbumType albumType;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    private String imageKey;

    @NotEmpty
    private Set<ArtistWithRole> artists;

    private Boolean explicit;

    private List<TrackCreateDto> tracks;

}