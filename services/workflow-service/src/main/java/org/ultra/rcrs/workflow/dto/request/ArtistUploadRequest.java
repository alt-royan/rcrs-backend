package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.NotBlank;
import org.ultra.rcrs.workflow.dto.SocialLinkDto;

import java.util.List;

public record ArtistUploadRequest(
        @NotBlank
        String name,
        String avatarUri,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<SocialLinkDto> socialLinks,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        List<String> tags
) {
}
