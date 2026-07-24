package org.ultra.rcrs.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserProfileResponse(
        String username,
        String avatarKey,
        String email,
        String firstName,
        String lastName,
        Boolean enabled,
        Boolean emailVerified
) {
}
