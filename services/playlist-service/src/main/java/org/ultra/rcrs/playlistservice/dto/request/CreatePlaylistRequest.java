package org.ultra.rcrs.playlistservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.ultra.rcrs.playlistservice.model.PlaylistType;

import java.util.List;

@Data
@Schema(description = "Payload used to create a new playlist. The owner is not part of this payload " +
        "and is instead derived from the authenticated caller's JWT subject.")
public class CreatePlaylistRequest {

    @NotBlank
    @Schema(description = "Display title of the playlist.", example = "Road Trip Mix", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "Free-text description of the playlist.", example = "Songs for long summer drives")
    private String description;

    @Schema(description = "Free-form tags used to categorize/search the playlist.")
    private List<String> tags;

    @Schema(description = "Short (Base62) track IDs to seed the playlist with at creation time, in order.")
    private List<String> trackIds;

    @Schema(description = "URI/key of the cover image as returned by the media service; parsed into a storage key server-side.")
    private String coverUri;

    @Schema(description = "Whether the playlist is private to its owner (true) or visible to others (false).", defaultValue = "true")
    private boolean isPrivate = true;

    @Schema(description = "Type/category of the playlist.", defaultValue = "CUSTOM")
    private PlaylistType type = PlaylistType.CUSTOM;
}
