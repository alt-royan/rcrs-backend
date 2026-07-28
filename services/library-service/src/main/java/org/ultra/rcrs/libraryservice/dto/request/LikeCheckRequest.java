package org.ultra.rcrs.libraryservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Batch query asking which of the supplied entities the caller has liked. " +
        "Intended to be called once per screen rather than once per item.")
public class LikeCheckRequest {

    @NotEmpty
    @Size(max = 200, message = "must contain at most 200 items")
    @Valid
    @Schema(description = "Entities to check. Types may be mixed freely within one request.")
    private List<Item> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(description = "A single entity to check.")
    public static class Item {

        @NotNull
        @Schema(description = "Kind of entity.", example = "TRACK")
        private LibraryEntityType type;

        @NotNull
        @Schema(description = "Base62-encoded short identifier of the entity.")
        private String id;
    }
}
