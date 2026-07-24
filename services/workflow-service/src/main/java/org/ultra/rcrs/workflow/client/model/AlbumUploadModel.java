package org.ultra.rcrs.workflow.client.model;

import org.ultra.rcrs.enums.AlbumType;

import java.time.OffsetDateTime;

public record AlbumUploadModel(
        String title,
        AlbumType type,
        OffsetDateTime releaseDate,
        OffsetDateTime publishTimestamp,
        String coverUri
) {
}