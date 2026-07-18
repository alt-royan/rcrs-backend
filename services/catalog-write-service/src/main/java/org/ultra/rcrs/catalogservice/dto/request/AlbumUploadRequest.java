package org.ultra.rcrs.catalogservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.ultra.rcrs.enums.AlbumType;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AlbumUploadRequest {

    @NotNull
    private String title;

    @NotNull
    private AlbumType type;

    private OffsetDateTime releaseDate;

    private OffsetDateTime publishTimestamp;

    private String coverUri;
}
