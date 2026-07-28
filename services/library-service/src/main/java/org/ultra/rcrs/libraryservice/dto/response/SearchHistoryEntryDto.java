package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.time.Instant;

@Schema(description = "One entity the caller previously opened from search results.")
public record SearchHistoryEntryDto(
        @Schema(description = "Kind of entity.", example = "ARTIST") LibraryEntityType entityType,
        @Schema(description = "Base62-encoded short identifier of the entity.") String entityId,
        @Schema(description = "Query that led to this entity, if the client reported one.") String query,
        @Schema(description = "When it was most recently opened, as an ISO-8601 UTC instant.") Instant searchedAt) {
}
