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
import org.ultra.rcrs.libraryservice.dto.request.LikeCheckRequest;
import org.ultra.rcrs.libraryservice.dto.response.LikeSummaryResponse;
import org.ultra.rcrs.libraryservice.dto.response.LikedEntityDto;
import org.ultra.rcrs.libraryservice.dto.response.PaginationResponse;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.service.LikeService;
import org.ultra.rcrs.security.CallerId;

import java.util.Map;

@Tag(
        name = "Likes",
        description = "The caller's liked tracks, albums, artists and playlists. A like is not a playlist: " +
                "it has no ordering and no membership operations, which is why liking any of the four " +
                "entity kinds is the same call shape. Responses carry identifiers only — titles and " +
                "artwork are fetched separately from the catalog."
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/me/library/likes")
public class LikeController {

    private final LikeService likeService;

    @Operation(
            summary = "List the caller's likes of one entity kind",
            description = "Returns the caller's liked entities of the given kind, newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The page of liked entity identifiers"),
            @ApiResponse(responseCode = "400", description = "Unknown entity kind, or offset/limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{type}")
    public PaginationResponse<LikedEntityDto> list(
            @Parameter(description = "Entity kind, as a plural path segment", required = true,
                    schema = @Schema(allowableValues = {"tracks", "albums", "artists", "playlists"}))
            @PathVariable("type") String type,
            @Parameter(description = "Zero-based index of the first item to return")
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Maximum number of items to return")
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return likeService.list(CallerId.of(jwt), LibraryEntityType.fromPathSegment(type), offset, limit);
    }

    @Operation(
            summary = "Like an entity",
            description = "Idempotent. Liking something the caller already likes leaves the original " +
                    "timestamp untouched, so a retried request does not reorder their library."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entity is liked"),
            @ApiResponse(responseCode = "400", description = "Unknown entity kind",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{type}/{id}")
    public ResponseEntity<Void> like(
            @Parameter(description = "Entity kind, as a plural path segment", required = true)
            @PathVariable("type") String type,
            @Parameter(description = "Base62-encoded short identifier of the entity", required = true)
            @PathVariable("id") String id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        likeService.like(CallerId.of(jwt), LibraryEntityType.fromPathSegment(type), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Remove a like",
            description = "Idempotent. Unliking something that is not liked succeeds and changes nothing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entity is not liked"),
            @ApiResponse(responseCode = "400", description = "Unknown entity kind",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Void> unlike(
            @Parameter(description = "Entity kind, as a plural path segment", required = true)
            @PathVariable("type") String type,
            @Parameter(description = "Base62-encoded short identifier of the entity", required = true)
            @PathVariable("id") String id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        likeService.unlike(CallerId.of(jwt), LibraryEntityType.fromPathSegment(type), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Check a batch of entities for likes",
            description = "Answers a whole screen's worth of heart icons in one request. Entity kinds may " +
                    "be mixed freely; keys in the response are of the form TYPE:id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Whether the caller likes each requested entity"),
            @ApiResponse(responseCode = "400", description = "Empty batch, or more than 200 items",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/check")
    public Map<String, Boolean> check(
            @Valid @RequestBody LikeCheckRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return likeService.check(CallerId.of(jwt), request);
    }

    @Operation(
            summary = "Count the caller's likes by entity kind",
            description = "Totals for the library landing tiles."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Per-kind like counts"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/summary")
    public LikeSummaryResponse summary(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return likeService.summary(CallerId.of(jwt));
    }
}
