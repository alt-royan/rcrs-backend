package org.ultra.rcrs.metadata.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Mono;

@Tag(name = "Track Admin", description = "Administrative endpoints for browsing tracks with unfiltered lifecycle and availability data.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/admin/tracks")
public class TrackAdminController {

    private final TrackAdminService trackAdminService;

    @Operation(summary = "Get a track by ID (admin)", description = "Returns the unfiltered admin detail view of a single track, regardless of its lifecycle/availability status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Track found"),
            @ApiResponse(responseCode = "404", description = "Track not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{trackId}")
    public Mono<TrackAdminViewDto> getTrack(
            @Parameter(description = "Base62-encoded short ID of the track.") @PathVariable("trackId") String trackId) {
        return trackAdminService.getById(trackId);
    }

    @Operation(summary = "Search/list tracks (admin)", description = "Returns a paginated list of tracks, optionally filtered by title, availability status, lifecycle status, containing album and explicitness.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tracks returned")
    })
    @GetMapping
    public Mono<PaginationResponse<TrackAdminStandaloneDto>> getTracks(
            @Parameter(description = "Case-insensitive filter on track title.") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by availability/visibility status (e.g. ACTIVE, HIDDEN, DELETED).") @RequestParam(required = false) EntityStatus availabilityStatus,
            @Parameter(description = "Filter by editorial lifecycle status (e.g. DRAFT, PUBLISHED, ARCHIVED).") @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @Parameter(description = "Filter by the Base62-encoded short ID of the containing album.") @RequestParam(required = false) String albumId,
            @Parameter(description = "Filter by explicit-content flag.") @RequestParam(required = false) Boolean explicit,
            @Parameter(description = "Zero-based offset of the first item to return.") @RequestParam(required = false, defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of items to return.") @RequestParam(required = false, defaultValue = "50") int limit,
            @Parameter(description = "Sort direction: \"asc\" or \"desc\".") @RequestParam(required = false, defaultValue = "asc") String sort) {
        return trackAdminService.getAll(title, availabilityStatus, lifecycleStatus, albumId, explicit, offset, limit, sort);
    }
}
