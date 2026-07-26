package org.ultra.rcrs.mediaservice.dao.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AudioWithTrack(
        UUID id,
        UUID guid,
        String key,
        String codec,
        String container,
        Integer durationMs,
        String bitrate,
        String sampleRate,
        Long byteSize,
        OffsetDateTime creationTimestamp,
        String trackId,
        Boolean main
) {
}
