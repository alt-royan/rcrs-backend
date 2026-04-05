package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.ultra.rcrs.enums.AlbumType;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Validated
@Data
public class AlbumCreateRequest {

    @NotNull
    private String title;

    @NotNull
    private AlbumType type;

    @NotNull
    private OffsetDateTime releaseDate;

    //Ссылки на s3 приходят в формате s3://{bucket}/{key}
    @NotNull
    @Pattern(regexp = "s3://[\\w\\-]+/[\\w\\-.]+", message = "URI must be s3://{bucket}/{key} formatted")
    private String coverUri;

    @NotEmpty
    @Valid
    private Set<ArtistId> artists = new HashSet<>();

    @NotNull
    private Boolean explicit;

    @NotNull
    @Valid
    private List<TrackCreateRequest> tracks = new ArrayList<>();

}