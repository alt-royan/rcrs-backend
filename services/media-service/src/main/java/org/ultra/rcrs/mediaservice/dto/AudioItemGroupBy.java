package org.ultra.rcrs.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "All audio asset variants that were produced from the same original upload, grouped together.")
public record AudioItemGroupBy(
        @Schema(description = "Identifier shared by all variants in this group")
        UUID guid,
        @Schema(description = "Whether this group is the primary/default one for its track")
        Boolean main,
        @Schema(description = "The individual encoded audio variants belonging to this group")
        List<AudioItem> items
) {
}
