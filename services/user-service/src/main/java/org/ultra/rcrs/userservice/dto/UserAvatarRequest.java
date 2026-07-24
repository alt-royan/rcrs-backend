package org.ultra.rcrs.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UserAvatarRequest(
        @JsonProperty("avatar")
        @NotBlank
        String avatar
) {
}
