package org.ultra.rcrs.playlistservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "A single track entry within a playlist, including its ordering position.")
public class PlaylistTrackViewDto {

    @Schema(description = "Short (Base62) ID of the track.", example = "2CwA8u")
    private String trackId;

    @Schema(description = "Zero-based (or service-defined) ordering position of the track within the playlist.", example = "0")
    private int position;

    @Schema(description = "Timestamp at which the track was added to the playlist.")
    private Instant addedAt;
}
