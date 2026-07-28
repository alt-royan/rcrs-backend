package org.ultra.rcrs.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.userservice.dto.ErrorResponse;
import org.ultra.rcrs.userservice.dto.UserAvatarRequest;
import org.ultra.rcrs.userservice.service.UserAvatarService;

import java.security.Principal;

@RestController
@RequestMapping("/me/avatar")
@RequiredArgsConstructor
@Tag(name = "User Avatar", description = "Endpoint for uploading the avatar of the currently authenticated user")
public class UserAvatarController {

    private final UserAvatarService userAvatarService;

    @PostMapping
    @Operation(
            summary = "Upload avatar for the current user",
            description = "Saves the avatar for the caller identified by the authenticated principal. The avatar " +
                    "always belongs to the currently authenticated user - there is no way to upload an avatar on " +
                    "behalf of an arbitrary user id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Avatar uploaded and saved successfully"),
            @ApiResponse(responseCode = "400", description = "Request body is missing, malformed, or fails validation " +
                    "(e.g. blank avatar value)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No local user record exists for the authenticated principal",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> uploadAvatar(@RequestBody @Valid UserAvatarRequest request,
                                             Principal principal) {
        userAvatarService.saveAvatar(principal.getName(), request.avatar());
        return ResponseEntity.noContent().build();
    }
}
