package org.ultra.rcrs.mediaservice.temporal.activity.model;

import java.io.Serializable;

public record ValidatedImage(
        String format,
        String contentType,
        String key,
        byte[] imageData
) implements Serializable {
}
