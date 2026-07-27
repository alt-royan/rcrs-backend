package org.ultra.rcrs.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profile information for a user, as synchronized from Keycloak identity events")
public record UserProfileResponse(
        @Schema(description = "Internal user identifier, matching the subject claim of the user's JWT")
        String userId,
        @Schema(description = "Username of the user, as registered in Keycloak")
        String username,
        @Schema(description = "Publicly accessible URL of the user's avatar image, or null if none has been uploaded")
        String avatarUrl,
        @Schema(description = "Email address of the user, as registered in Keycloak")
        String email,
        @Schema(description = "Whether the user's account is currently enabled")
        Boolean enabled,
        @Schema(description = "Whether the user's email address has been verified")
        Boolean emailVerified
) {
}
