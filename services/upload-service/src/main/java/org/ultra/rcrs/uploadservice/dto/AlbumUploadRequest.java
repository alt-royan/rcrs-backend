package org.ultra.rcrs.uploadservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

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
    private Set<ArtistId> artists;

    @Valid
    private List<TrackUploadRequest> tracks;
}
