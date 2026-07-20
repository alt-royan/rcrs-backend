package org.ultra.rcrs.workflow.client.model;

import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.workflow.dto.ArtistDto;
import org.ultra.rcrs.workflow.dto.request.TrackUploadRequest;

import java.time.OffsetDateTime;
import java.util.List;

public record AlbumUploadModel(
        String title,
        AlbumType type,
        OffsetDateTime releaseDate,
        OffsetDateTime publishTimestamp,
        String coverUri
) {
}