package org.ultra.rcrs.mediaservice.temporal.activity.model;

import java.io.Serializable;

public record ValidatedAudio(
        String uid,
        String format,
        double durationSeconds
) implements Serializable {
}
