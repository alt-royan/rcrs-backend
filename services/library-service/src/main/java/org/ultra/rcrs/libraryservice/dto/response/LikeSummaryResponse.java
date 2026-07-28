package org.ultra.rcrs.libraryservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How many entities of each kind the caller has liked, for the library landing tiles.")
public record LikeSummaryResponse(
        @Schema(description = "Number of liked tracks.") long tracks,
        @Schema(description = "Number of liked albums.") long albums,
        @Schema(description = "Number of liked artists.") long artists,
        @Schema(description = "Number of liked playlists.") long playlists) {
}
