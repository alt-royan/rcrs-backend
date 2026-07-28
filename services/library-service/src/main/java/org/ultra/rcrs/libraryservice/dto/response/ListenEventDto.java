package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.PlaybackSource;

import java.time.Instant;

@Schema(description = "One entry in the caller's raw playback timeline.")
public record ListenEventDto(
        @Schema(description = "Base62-encoded short identifier of the track that was played.") String trackId,
        @Schema(description = "Base62-encoded short identifier of the track's album, if it was reported.") String albumId,
        @Schema(description = "When playback started, as an ISO-8601 UTC instant.") Instant playedAt,
        @Schema(description = "How many milliseconds of the track were played.") int msPlayed,
        @Schema(description = "Whether the play reached the completion threshold.") boolean completed,
        @Schema(description = "What the play was started from, if it was reported.") PlaybackSource sourceType,
        @Schema(description = "Identifier of the album, playlist or artist named by sourceType.") String sourceId) {
}
