package org.ultra.rcrs.workflow.dto.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import org.ultra.rcrs.workflow.dto.SocialLinkDto;

import java.util.List;

@Schema(description = "Request body for registering a new artist through the orchestrated artist registration workflow.")
public record ArtistUploadRequest(
        @NotBlank
        @Schema(description = "Display name of the artist to register.", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,
        @Schema(description = "URI of the artist's avatar/profile image, if already uploaded.")
        String avatarUri,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Social media links for the artist; missing/null is treated as an empty list.")
        List<SocialLinkDto> socialLinks,
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @Schema(description = "Freeform genre/style tags associated with the artist; missing/null is treated as an empty list.")
        List<String> tags
) {
}
