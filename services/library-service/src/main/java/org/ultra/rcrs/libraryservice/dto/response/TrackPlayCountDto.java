package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "How much the caller has played one track.")
public record TrackPlayCountDto(
        @Schema(description = "Base62-encoded short identifier of the track.") String trackId,
        @Schema(description = "Number of recorded playbacks.") long playCount,
        @Schema(description = "Total milliseconds played across all of them.") long msPlayed,
        @Schema(description = "When it was last played, as an ISO-8601 UTC instant.") Instant lastPlayedAt) {
}
