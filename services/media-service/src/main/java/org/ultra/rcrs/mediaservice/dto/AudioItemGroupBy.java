package org.ultra.rcrs.mediaservice.dto;

import java.util.List;
import java.util.UUID;

public record AudioItemGroupBy(
        UUID guid,
        Boolean main,
        List<AudioItem> items
) {
}
