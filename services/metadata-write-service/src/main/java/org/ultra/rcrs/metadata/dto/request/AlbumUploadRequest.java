package org.ultra.rcrs.metadata.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.ultra.rcrs.enums.AlbumType;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class AlbumUploadRequest {

    @NotNull
    private String title;

    @NotNull
    private AlbumType type;

    @NotNull
    private LocalDateTime releaseDate;

    private OffsetDateTime publishTimestamp;

    private String coverUri;
}
