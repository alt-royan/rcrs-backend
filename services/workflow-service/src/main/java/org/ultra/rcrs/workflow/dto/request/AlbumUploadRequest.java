package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.workflow.dto.ArtistDto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request body for uploading a new album, including its tracks and artist relationships, " +
        "through the orchestrated album upload workflow.")
public record AlbumUploadRequest(
        @NotNull
        @Schema(description = "Title of the album.", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotNull
        @Schema(description = "Type of the album (e.g. single, EP, LP).", requiredMode = Schema.RequiredMode.REQUIRED)
        AlbumType type,
        @Schema(description = "Calendar release date of the album, ISO-8601 (yyyy-MM-dd).", example = "2024-03-01")
        LocalDate releaseDate,
        @Schema(description = "Instant at which the album should become publicly visible, ISO-8601 UTC.", example = "2024-03-01T00:00:00Z")
        Instant publishTimestamp,
        @Schema(description = "URI of the album's cover artwork, if already uploaded.")
        String coverUri,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Primary artists credited on the album; missing/null is treated as an empty list.")
        List<ArtistDto> artists,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Tracks to create as part of this album upload; missing/null is treated as an empty list.")
        List<TrackUploadRequest> tracks) {
}