package org.ultra.rcrs.libraryservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.libraryservice.dto.ErrorResponse;
import org.ultra.rcrs.libraryservice.dto.request.DownloadCollectionRequest;
import org.ultra.rcrs.libraryservice.dto.request.IdsRequest;
import org.ultra.rcrs.libraryservice.dto.response.DownloadedCollectionDto;
import org.ultra.rcrs.libraryservice.dto.response.DownloadedTrackDto;
import org.ultra.rcrs.libraryservice.dto.response.PaginationResponse;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.Quality;
import org.ultra.rcrs.libraryservice.service.DownloadService;

import java.util.Map;

@Tag(
        name = "Downloads",
        description = "A record of what the caller has downloaded to their device, kept purely so the " +
                "library can render it. This service issues no entitlement and applies no expiry, and it " +
                "never contacts media-service: presigned download URLs are obtained from " +
                "media-service directly. Files stay on the device until the user removes them."
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/me/library/downloads")
public class DownloadController {

    private final DownloadService downloadService;

    @Operation(
            summary = "List every downloaded track",
            description = "The flat list of track files on the device, newest first. This is what an " +
                    "offline player reads."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The page of downloaded track identifiers"),
            @ApiResponse(responseCode = "400", description = "offset or limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/tracks")
    public PaginationResponse<DownloadedTrackDto> listTracks(
            @Parameter(description = "Zero-based index of the first item to return")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Maximum number of items to return")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return downloadService.listTracks(jwt.getSubject(), offset, limit);
    }

    @Operation(
            summary = "List downloaded albums and playlists",
            description = "What the user believes they downloaded, so the Downloads screen can show one " +
                    "card per album or playlist rather than every track loose."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The page of downloaded collections"),
            @ApiResponse(responseCode = "400", description = "offset or limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/collections")
    public PaginationResponse<DownloadedCollectionDto> listCollections(
            @Parameter(description = "Zero-based index of the first item to return")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Maximum number of items to return")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return downloadService.listCollections(jwt.getSubject(), offset, limit);
    }

    @Operation(
            summary = "Record a single downloaded track",
            description = "Idempotent. Re-recording the same track at a different quality replaces the " +
                    "existing entry, since there is one file per track on the device. Tracks recorded " +
                    "this way are kept even when every collection containing them is removed."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The track is recorded as downloaded"),
            @ApiResponse(responseCode = "400", description = "Unknown quality",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/tracks/{trackId}")
    public ResponseEntity<Void> addTrack(
            @Parameter(description = "Base62-encoded short identifier of the track", required = true)
            @PathVariable("trackId") String trackId,
            @Parameter(description = "Audio quality the file was downloaded at", required = true)
            @RequestParam Quality quality,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        downloadService.addTrack(jwt.getSubject(), trackId, quality);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Forget a downloaded track",
            description = "Idempotent. Also drops the track from any collection that listed it."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The track is no longer recorded as downloaded"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/tracks/{trackId}")
    public ResponseEntity<Void> removeTrack(
            @Parameter(description = "Base62-encoded short identifier of the track", required = true)
            @PathVariable("trackId") String trackId,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        downloadService.removeTrack(jwt.getSubject(), trackId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Record a downloaded album or playlist",
            description = "The client supplies the track ids, because this service holds no catalog data " +
                    "and makes no calls to other services. Re-posting the same collection replaces its " +
                    "track list, dropping any files no longer part of it and not claimed elsewhere."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The collection and its tracks are recorded"),
            @ApiResponse(responseCode = "400", description = "Entity kind is not an album or playlist, or the track list is empty or too large",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/collections")
    public ResponseEntity<Void> addCollection(
            @Valid @RequestBody DownloadCollectionRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        downloadService.addCollection(jwt.getSubject(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Forget a downloaded album or playlist",
            description = "Removes the collection and the files it was the last claim on. Tracks the user " +
                    "downloaded individually, or that another downloaded collection still lists, stay."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The collection is no longer recorded as downloaded"),
            @ApiResponse(responseCode = "400", description = "Unknown entity kind",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/collections/{type}/{id}")
    public ResponseEntity<Void> removeCollection(
            @Parameter(description = "Collection kind, as a plural path segment", required = true,
                    schema = @Schema(allowableValues = {"albums", "playlists"}))
            @PathVariable("type") String type,
            @Parameter(description = "Base62-encoded short identifier of the album or playlist", required = true)
            @PathVariable("id") String id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        downloadService.removeCollection(jwt.getSubject(), LibraryEntityType.fromPathSegment(type), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Check a batch of tracks for downloads",
            description = "Backs the download badges on a track list. Each requested track maps to the " +
                    "quality it is held at, or null when it is not downloaded."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Download state of each requested track"),
            @ApiResponse(responseCode = "400", description = "Empty batch, or more than 200 ids",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/check")
    public Map<String, Quality> check(
            @Valid @RequestBody IdsRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return downloadService.check(jwt.getSubject(), request.getIds());
    }
}
