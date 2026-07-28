package org.ultra.rcrs.libraryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Records that the caller opened an entity from search results. " +
        "Posted on the tap, not while the user is typing.")
public class SearchHistoryRequest {

    @NotNull
    @Schema(description = "Kind of entity that was opened.", example = "ALBUM")
    private LibraryEntityType entityType;

    @NotBlank
    @Schema(description = "Base62-encoded short identifier of the entity that was opened.")
    private String entityId;

    @Size(max = 512)
    @Schema(description = "The search query that led to this entity, when the client knows it. " +
            "Optional, because the entity may have been reached without searching.")
    private String query;
}
