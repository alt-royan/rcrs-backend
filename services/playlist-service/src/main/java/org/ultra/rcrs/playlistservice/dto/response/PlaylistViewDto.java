package org.ultra.rcrs.playlistservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ImageSize;
import org.ultra.rcrs.playlistservice.model.PlaylistType;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Public view of a playlist and its metadata.")
public class PlaylistViewDto {

    @Schema(description = "Short (Base62) ID of the playlist.", example = "1BvZ7t")
    private String id;

    @Schema(description = "Subject (user) ID of the playlist owner, taken from the owner's JWT at creation time.")
    private String ownerId;

    @Schema(description = "Display title of the playlist.", example = "Road Trip Mix")
    private String title;

    @Schema(description = "Free-text description of the playlist.")
    private String description;

    @Schema(description = "Free-form tags associated with the playlist.")
    private List<String> tags;

    @Schema(description = "URLs of the playlist cover image, keyed by thumbnail size (SM/MD/LG), built from the stored storage key. Empty if no cover has been set.")
    private Map<ImageSize, URI> cover;

    @Schema(description = "Whether the playlist is private to its owner.")
    private Boolean isPrivate;

    @Schema(description = "Type/category of the playlist.")
    private PlaylistType type;

    @Schema(description = "Number of tracks currently in the playlist.")
    private Integer trackCount;

    @Schema(description = "Timestamp at which the playlist was created.")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp at which the playlist was last updated.")
    private LocalDateTime updatedAt;
}
