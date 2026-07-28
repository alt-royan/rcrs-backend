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
import org.ultra.rcrs.libraryservice.dto.request.SearchHistoryRequest;
import org.ultra.rcrs.libraryservice.dto.response.SearchHistoryEntryDto;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.service.SearchHistoryService;
import org.ultra.rcrs.security.CallerId;

import java.util.List;

@Tag(
        name = "Search history",
        description = "Entities the caller opened from search results, newest first — the 'recent' cards " +
                "shown under the search bar. An entry is written when a result is tapped, not while the " +
                "user is typing, so this is a list of entities rather than of query strings. The query " +
                "that led to each tap is recorded alongside it when the client reports one."
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/me/library/search-history")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @Operation(
            summary = "List recently opened search results",
            description = "Returns distinct entities, most recently opened first. Re-opening an entity " +
                    "moves it to the top rather than adding a duplicate."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The recently opened entities"),
            @ApiResponse(responseCode = "400", description = "limit out of range",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public List<SearchHistoryEntryDto> list(
            @Parameter(description = "Maximum number of entries to return")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        return searchHistoryService.list(CallerId.of(jwt), limit);
    }

    @Operation(
            summary = "Record that a search result was opened",
            description = "Call this when the user taps a result. Recording the same entity again updates " +
                    "the existing entry instead of adding another. The list is capped per user, with the " +
                    "oldest entries dropped."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry is recorded"),
            @ApiResponse(responseCode = "400", description = "The request body failed validation",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Void> record(
            @Valid @RequestBody SearchHistoryRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        searchHistoryService.record(CallerId.of(jwt), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Clear the whole search history", description = "Idempotent.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The search history is empty"),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> clear(@Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        searchHistoryService.clear(CallerId.of(jwt));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove one search history entry", description = "Idempotent.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The entry is gone"),
            @ApiResponse(responseCode = "400", description = "Unknown entity kind",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "The caller is not authenticated",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{type}/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Entity kind, as a plural path segment", required = true,
                    schema = @Schema(allowableValues = {"tracks", "albums", "artists", "playlists"}))
            @PathVariable("type") String type,
            @Parameter(description = "Base62-encoded short identifier of the entity", required = true)
            @PathVariable("id") String id,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {
        searchHistoryService.delete(CallerId.of(jwt), LibraryEntityType.fromPathSegment(type), id);
        return ResponseEntity.noContent().build();
    }
}
