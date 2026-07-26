package org.ultra.rcrs.userservice.dto;

public record UserProfileResponse(
        String userId,
        String username,
        String avatarUrl,
        String email,
        Boolean enabled,
        Boolean emailVerified
) {
}
