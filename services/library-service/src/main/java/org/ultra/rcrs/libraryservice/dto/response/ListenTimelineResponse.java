package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Keyset-paginated rather than offset-paginated: the timeline is backed by an
 * append-only collection, where a deep offset means walking every newer document.
 */
@Schema(description = "A page of the caller's playback timeline, paginated by cursor rather than offset.")
public record ListenTimelineResponse(
        @Schema(description = "Playbacks in this page, newest first.") List<ListenEventDto> items,
        @Schema(description = "Pass this back as the cursor to fetch the next, older page. " +
                "Null when there are no older entries.") Instant nextCursor) {
}
