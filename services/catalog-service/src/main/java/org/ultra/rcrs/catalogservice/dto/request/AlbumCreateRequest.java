package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Validated
@Data
public class AlbumCreateRequest {

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

    @NotNull
    private Boolean explicit;

    @NotNull
    @Valid
    private List<TrackCreateRequest> tracks;

}