package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import org.ultra.rcrs.workflow.dto.ArtistDto;
import org.ultra.rcrs.workflow.dto.OtherArtistDto;

import java.util.List;

public record TrackUploadRequest(
        @NotBlank
        String uid,
        @NotBlank
        String title,
        @NotBlank
        Integer trackNumber,
        Boolean explicit,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<ArtistDto> artists,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<OtherArtistDto> others
) {
}
