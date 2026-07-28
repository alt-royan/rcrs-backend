package org.ultra.rcrs.metadata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ultra.rcrs.metadata.dto.ArtistDto;

import java.util.List;

@Schema(description = "Payload for attaching or detaching main/featured artists to/from an album or track.")
@Data
public class ArtistsToEntityRequest {

    @Schema(description = "List of artists (with their role) to add to or remove from the entity. Must not be empty.")
    @NotEmpty
    @Valid
    private List<ArtistDto> artists;
}
