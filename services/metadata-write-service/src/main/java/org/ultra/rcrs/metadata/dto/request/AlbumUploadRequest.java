package org.ultra.rcrs.metadata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Payload for creating a new album.")
@Data
public class AlbumUploadRequest {

    @Schema(description = "Title of the album. Required.", example = "Abbey Road")
    @NotNull
    private String title;

    @Schema(description = "Type of album release. Valid values: FULL (full-length album), SINGLE, EP. Required.")
    @NotNull
    private AlbumType type;

    @Schema(description = "Calendar date the album was/will be released, ISO-8601 (yyyy-MM-dd). Required.", example = "2024-03-01")
    @NotNull
    private LocalDate releaseDate;

    @Schema(description = "Instant at which the album should become publicly visible/published, ISO-8601 UTC. Optional.", example = "2024-03-01T00:00:00Z")
    private Instant publishTimestamp;

    @Schema(description = "URI/key of the album cover image previously uploaded to storage. Optional.")
    private String coverUri;
}
