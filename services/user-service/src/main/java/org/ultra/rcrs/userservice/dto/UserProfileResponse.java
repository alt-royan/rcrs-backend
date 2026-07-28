package org.ultra.rcrs.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.ultra.rcrs.enums.ImageSize;

import java.net.URI;
import java.util.Map;

@Schema(description = "Profile information for a user, as synchronized from Keycloak identity events")
public record UserProfileResponse(
        @Schema(description = "Internal user identifier, matching the subject claim of the user's JWT")
        String userId,
        @Schema(description = "Username of the user, as registered in Keycloak")
        String username,
        @Schema(description = "URLs of the user's avatar image, keyed by thumbnail size (SM/MD/LG); empty if none has been uploaded")
        Map<ImageSize, URI> avatar,
        @Schema(description = "Email address of the user, as registered in Keycloak")
        String email,
        @Schema(description = "Whether the user's account is currently enabled")
        Boolean enabled,
        @Schema(description = "Whether the user's email address has been verified")
        Boolean emailVerified
) {
}
