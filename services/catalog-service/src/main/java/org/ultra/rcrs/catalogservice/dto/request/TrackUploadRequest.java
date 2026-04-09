package org.ultra.rcrs.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.catalogservice.dto.ArtistOtherDto;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Validated
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
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ArtistIdDto> artists;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ArtistOtherDto> others;
}
