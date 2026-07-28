package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.time.Instant;

@Schema(description = "One entry on the 'recently played' shelf.")
public record RecentlyPlayedDto(
        @Schema(description = "Kind of entity.", example = "ALBUM") LibraryEntityType entityType,
        @Schema(description = "Base62-encoded short identifier of the entity.") String entityId,
        @Schema(description = "When the caller last played it, as an ISO-8601 UTC instant.") Instant lastPlayedAt,
        @Schema(description = "How many playbacks have been attributed to it.") long playCount) {
}
