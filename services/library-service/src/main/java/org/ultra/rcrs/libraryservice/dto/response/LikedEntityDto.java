package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.time.Instant;

@Schema(description = "One liked entity. Only identifiers are returned; titles and artwork are " +
        "fetched separately from the catalog by the client.")
public record LikedEntityDto(
        @Schema(description = "Kind of entity.", example = "ALBUM") LibraryEntityType type,
        @Schema(description = "Base62-encoded short identifier of the entity.") String id,
        @Schema(description = "When the caller liked it, as an ISO-8601 UTC instant.") Instant likedAt) {
}
