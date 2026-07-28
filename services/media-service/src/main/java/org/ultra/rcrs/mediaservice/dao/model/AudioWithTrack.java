package org.ultra.rcrs.mediaservice.dao.model;

import org.ultra.rcrs.mediaservice.enums.Quality;

import java.time.Instant;
import java.util.UUID;

public record AudioWithTrack(
        UUID id,
        UUID guid,
        String key,
        String codec,
        String container,
        String contentType,
        Integer durationMs,
        String bitrate,
        Quality quality,
        String sampleRate,
        Long byteSize,
        Instant creationTimestamp,
        String trackId,
        Boolean main
) {
}
