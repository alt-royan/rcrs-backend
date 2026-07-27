package org.ultra.rcrs.workflow.client.model;

import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.time.LocalDate;

public record AlbumUploadModel(
        String title,
        AlbumType type,
        LocalDate releaseDate,
        Instant publishTimestamp,
        String coverUri
) {
}