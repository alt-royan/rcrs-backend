package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.ultra.rcrs.workflow.dto.ArtistDto;
import org.ultra.rcrs.workflow.dto.OtherArtistDto;

import java.util.List;

@Schema(description = "A single track to create as part of an album upload workflow.")
public record TrackUploadRequest(
        @NotBlank
        @Schema(description = "Identifier of the already-uploaded audio file (media-service) that this track is backed by.", requiredMode = Schema.RequiredMode.REQUIRED)
        String uid,
        @NotBlank
        @Schema(description = "Title of the track.", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @NotBlank
        @Schema(description = "Position of the track within the album's tracklist.", requiredMode = Schema.RequiredMode.REQUIRED)
        Integer trackNumber,
        @Schema(description = "Whether the track contains explicit content.")
        Boolean explicit,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Primary artists credited on the track; missing/null is treated as an empty list.")
        List<ArtistDto> artists,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Other (featured/contributing) artists credited on the track; missing/null is treated as an empty list.")
        List<OtherArtistDto> others
) {
}
