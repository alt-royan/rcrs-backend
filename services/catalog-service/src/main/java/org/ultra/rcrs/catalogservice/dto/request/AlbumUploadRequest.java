package org.ultra.rcrs.catalogservice.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.util.List;

@Validated
@Data
public class AlbumUploadRequest {


    @NotNull
    private String title;

    @NotNull
    private AlbumType type;

    private Instant releaseDate;

    //Ссылки на s3 приходят в формате s3://{bucket}/{key}
    @NotNull
    @Pattern(regexp = "s3://[\\w\\-]+/[\\w\\-.]+", message = "URI must be s3://{bucket}/{key} formatted")
    private String coverUri;

    @NotEmpty
    @Valid
    private List<ArtistIdDto> artists;

    @NotNull
    private Boolean explicit;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<TrackUploadRequest> tracks;

}
