package org.ultra.rcrs.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for uploading the avatar of the currently authenticated user")
public record UserAvatarRequest(
        @JsonProperty("avatar")
        @NotBlank
        @Schema(description = "Location of the avatar object in storage, expressed as an S3 URI or key " +
                "(e.g. \"s3://bucket/path/to/avatar.png\"), not raw base64 image data", requiredMode = Schema.RequiredMode.REQUIRED)
        String avatar
) {
}
