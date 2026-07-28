package org.ultra.rcrs.playlistservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "List of track IDs used to add or remove tracks from a playlist.")
public class TrackIdsRequest {

    @NotEmpty
    @Schema(description = "Short (Base62) track IDs to add to or remove from the playlist.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> trackIds;
}
