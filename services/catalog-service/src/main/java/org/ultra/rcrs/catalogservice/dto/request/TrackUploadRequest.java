package org.ultra.rcrs.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Data
public class TrackUploadRequest {

    @NotNull
    @NotEmpty
    private String uid;

    @NotNull
    private String title;

    @NotNull
    private Integer trackNumber;

    private Instant releaseDate;

    @NotNull
    private Boolean explicit;

    @NotEmpty
    @Valid
    private List<ArtistIdDto> artists = new ArrayList<>();

    @Valid
    private List<OtherArtistDto> others = new ArrayList<>();
}
