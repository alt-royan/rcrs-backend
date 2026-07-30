package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.Quality;

import java.time.Instant;

@Schema(description = "One track file the caller has on their device.")
public record DownloadedTrackDto(
        @Schema(description = "Base62-encoded short identifier of the track.") String trackId,
        @Schema(description = "Audio quality the file was downloaded at.", example = "HIGH") Quality quality,
        @Schema(description = "True when the track was downloaded on its own rather than as part " +
                "of an album or playlist. Such tracks are kept when those collections are removed.")
        boolean explicit,
        @Schema(description = "When it was downloaded, as an ISO-8601 UTC instant.") Instant addedAt) {
}
