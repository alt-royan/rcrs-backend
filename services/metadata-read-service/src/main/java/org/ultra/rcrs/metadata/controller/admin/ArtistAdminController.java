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
import org.ultra.rcrs.metadata.dto.AlbumAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.ArtistAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Artist Admin", description = "Administrative endpoints for browsing artists with unfiltered availability data.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/admin/artists")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;
    private final AlbumAdminService albumAdminService;

    @Operation(summary = "Get an artist by ID (admin)", description = "Returns the unfiltered admin detail view of a single artist, regardless of its availability status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artist found"),
            @ApiResponse(responseCode = "404", description = "Artist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{artistId}")
    public Mono<ArtistAdminViewDto> getArtist(
            @Parameter(description = "Base62-encoded short ID of the artist.") @PathVariable("artistId") String artistId) {
        return artistAdminService.getById(artistId);
    }

    @Operation(summary = "Search/list artists (admin)", description = "Returns a paginated list of artists, optionally filtered by name and availability status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of artists returned")
    })
    @GetMapping
    public Mono<PaginationResponse<ArtistAdminStandaloneDto>> getArtists(
            @Parameter(description = "Case-insensitive filter on artist name.") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by availability/visibility status (e.g. ACTIVE, HIDDEN, DELETED).") @RequestParam(required = false) EntityStatus availabilityStatus,
            @Parameter(description = "Zero-based offset of the first item to return.") @RequestParam(required = false, defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of items to return.") @RequestParam(required = false, defaultValue = "50") int limit) {
        return artistAdminService.getAll(name, availabilityStatus, offset, limit);
    }

    @Operation(summary = "List albums of an artist (admin)", description = "Returns the unfiltered admin albums credited to the given artist, optionally filtered by album type and sorted by release date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Albums returned (possibly empty)"),
            @ApiResponse(responseCode = "404", description = "Artist not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{artistId}/albums")
    public Flux<AlbumAdminStandaloneDto> getAlbumsByArtist(
            @Parameter(description = "Base62-encoded short ID of the artist.") @PathVariable("artistId") String artistId,
            @Parameter(description = "Optional album type filter (e.g. ALBUM, SINGLE, EP, COMPILATION).") @RequestParam(required = false) AlbumType type,
            @Parameter(description = "Sort direction by release date: \"asc\" or \"desc\".") @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumAdminService.getAllByArtistId(artistId, type, sort);
    }
}
