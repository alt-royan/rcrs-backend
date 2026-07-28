package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.Quality;

import java.time.Instant;

@Schema(description = "An album or playlist the caller downloaded as a unit.")
public record DownloadedCollectionDto(
        @Schema(description = "Kind of collection.", example = "ALBUM") LibraryEntityType entityType,
        @Schema(description = "Base62-encoded short identifier of the album or playlist.") String entityId,
        @Schema(description = "Audio quality the files were downloaded at.", example = "HIGH") Quality quality,
        @Schema(description = "How many tracks were downloaded with it.") int trackCount,
        @Schema(description = "When it was downloaded, as an ISO-8601 UTC instant.") Instant addedAt) {
}
