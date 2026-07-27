package org.ultra.rcrs.metadata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Schema(description = "Payload for creating a new album.")
@Data
public class AlbumUploadRequest {

    @Schema(description = "Title of the album. Required.", example = "Abbey Road")
    @NotNull
    private String title;

    @Schema(description = "Type of album release. Valid values: FULL (full-length album), SINGLE, EP. Required.")
    @NotNull
    private AlbumType type;

    @Schema(description = "Date the album was/will be released. Required.")
    @NotNull
    private LocalDateTime releaseDate;

    @Schema(description = "Timestamp at which the album should become publicly visible/published. Optional.")
    private OffsetDateTime publishTimestamp;

    @Schema(description = "URI/key of the album cover image previously uploaded to storage. Optional.")
    private String coverUri;
}
