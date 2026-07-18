package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TrackUploadRequest {

    @NotNull
    @NotEmpty
    private String uid;

    @NotNull
    private String albumId;

    @NotNull
    private String title;

    @NotNull
    private Integer trackNumber;

    @NotNull
    private OffsetDateTime releaseDate;

    private OffsetDateTime publishTimestamp;

    @NotNull
    private Boolean explicit;

    @NotEmpty
    @Valid
    private List<ArtistIdDto> artists = new ArrayList<>();

    @Valid
    private List<OtherArtistDto> others = new ArrayList<>();
}
