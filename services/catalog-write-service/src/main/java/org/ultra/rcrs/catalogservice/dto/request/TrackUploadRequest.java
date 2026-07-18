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

    private String id;

    private String albumId;

    private String title;

    private Integer trackNumber;

    private OffsetDateTime releaseDate;

    private OffsetDateTime publishTimestamp;

    private Boolean explicit;

    @Valid
    private List<ArtistDto> artists = new ArrayList<>();

    @Valid
    private List<OtherArtistDto> others = new ArrayList<>();
}
