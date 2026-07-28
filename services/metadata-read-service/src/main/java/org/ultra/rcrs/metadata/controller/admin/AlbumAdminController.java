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
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.*;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Album Admin", description = "Administrative endpoints for browsing albums with unfiltered lifecycle and availability data.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/admin/albums")
public class AlbumAdminController {

    private final AlbumAdminService albumAdminService;
    private final TrackAdminService trackAdminService;

    @Operation(summary = "Get an album by ID (admin)", description = "Returns the unfiltered admin detail view of a single album, regardless of its lifecycle/availability status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Album found"),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{albumId}")
    public Mono<AlbumAdminViewDto> getAlbum(
            @Parameter(description = "Base62-encoded short ID of the album.") @PathVariable("albumId") String albumId) {
        return albumAdminService.getById(albumId);
    }

    @Operation(summary = "Search/list albums (admin)", description = "Returns a paginated list of albums, optionally filtered by title, availability status, lifecycle status, album type and explicitness.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of albums returned")
    })
    @GetMapping
    public Mono<PaginationResponse<AlbumAdminStandaloneDto>> getAlbums(
            @Parameter(description = "Case-insensitive filter on album title.") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by availability/visibility status (e.g. ACTIVE, HIDDEN, DELETED).") @RequestParam(required = false) EntityStatus availabilityStatus,
            @Parameter(description = "Filter by editorial lifecycle status (e.g. DRAFT, PUBLISHED, ARCHIVED).") @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @Parameter(description = "Filter by album type (e.g. ALBUM, SINGLE, EP, COMPILATION).") @RequestParam(required = false) AlbumType type,
            @Parameter(description = "Filter by explicit-content flag.") @RequestParam(required = false) Boolean explicit,
            @Parameter(description = "Zero-based offset of the first item to return.") @RequestParam(required = false, defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of items to return.") @RequestParam(required = false, defaultValue = "50") int limit,
            @Parameter(description = "Sort direction: \"asc\" or \"desc\".") @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumAdminService.getAll(title, availabilityStatus, lifecycleStatus, type, explicit, offset, limit, sort);
    }

    @Operation(summary = "List tracks of an album (admin)", description = "Returns the unfiltered admin tracklist for the given album.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tracks returned (possibly empty)"),
            @ApiResponse(responseCode = "404", description = "Album not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{albumId}/tracks")
    public Flux<TrackAdminStandaloneDto> getTracksByAlbum(
            @Parameter(description = "Base62-encoded short ID of the album.") @PathVariable("albumId") String albumId) {
        return trackAdminService.getAllByAlbumId(albumId);
    }
}
