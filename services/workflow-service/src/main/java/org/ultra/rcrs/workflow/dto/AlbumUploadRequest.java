package org.ultra.rcrs.workflow.dto;

import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.AlbumType;

import java.time.OffsetDateTime;

public record AlbumUploadRequest(@NotNull String title, @NotNull AlbumType type, OffsetDateTime releaseDate,
                                 OffsetDateTime publishTimestamp, String coverUri) {
}