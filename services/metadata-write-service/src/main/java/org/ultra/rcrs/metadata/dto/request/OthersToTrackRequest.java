package org.ultra.rcrs.metadata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;

import java.util.List;

@Schema(description = "Payload for attaching or detaching non-performing contributors (e.g. producers, writers) to/from a track.")
@Data
public class OthersToTrackRequest {

    @Schema(description = "List of contributors to add to or remove from the track. Must not be empty.")
    @NotEmpty
    @Valid
    private List<OtherArtistDto> others;
}
