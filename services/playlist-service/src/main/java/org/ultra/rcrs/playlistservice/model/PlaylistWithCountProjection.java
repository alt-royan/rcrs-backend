package org.ultra.rcrs.playlistservice.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PlaylistWithCountProjection(
        UUID id,
        String ownerId,
        String title,
        String description,
        List<String> tags,
        String coverS3Key,
        Boolean isPrivate,
        PlaylistType type,
        Integer trackCount,
        Instant createdAt,
        Instant updatedAt) {
}
