package org.ultra.rcrs.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.ErrorResponse;
import org.ultra.rcrs.userservice.dto.UserProfileResponse;
import org.ultra.rcrs.userservice.service.UserAvatarService;
import org.ultra.rcrs.userservice.service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
@Tag(name = "User", description = "Endpoints for retrieving the profile of the currently authenticated user")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get compact profile of the current user",
            description = "Returns a compact representation of the profile belonging to the caller identified by " +
                    "the subject of the provided JWT access token. There is no concept of looking up an arbitrary " +
                    "user id here - the profile returned always corresponds to the authenticated caller."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compact profile returned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT access token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No local user record exists for the authenticated subject",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserProfileResponse> getCompactProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfileResponse profile = userService.getCompactProfile(jwt.getSubject());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile")
    @Operation(
            summary = "Get full profile of the current user",
            description = "Returns the full profile belonging to the caller identified by the subject of the " +
                    "provided JWT access token. There is no concept of looking up an arbitrary user id here - the " +
                    "profile returned always corresponds to the authenticated caller."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Full profile returned successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT access token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No local user record exists for the authenticated subject",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UserProfileResponse profile = userService.getProfile(jwt.getSubject());
        return ResponseEntity.ok(profile);
    }
}
