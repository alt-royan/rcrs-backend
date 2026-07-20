package org.ultra.rcrs.mediaservice.temporal.activity.model;

import java.io.Serializable;

public record AudioMetadata(
        String codec,
        String container,
        String bitrate,
        String sampleRate,
        long durationMs,
        long byteSize,
        String contentType
) implements Serializable {
}
