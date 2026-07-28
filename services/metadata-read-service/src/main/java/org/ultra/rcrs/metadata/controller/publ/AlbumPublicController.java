package org.ultra.rcrs.metadata.controller.publ;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.AlbumPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.AlbumPublicViewDto;
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.IdsRequest;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.service.publ.AlbumPublicService;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Album", description = "Public storefront endpoints for browsing publicly available albums and their tracks.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/albums")
public class AlbumPublicController {

    private final AlbumPublicService albumPublicService;
    private final TrackPublicService trackPublicService;

    @Operation(summary = "Get an album by ID", description = "Returns the public detail view of a single album.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Album found"),
            @ApiResponse(responseCode = "404", description = "Album not found or not publicly available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{albumId}")
    public Mono<AlbumPublicViewDto> getAlbum(
            @Parameter(description = "Base62-encoded short ID of the album.") @PathVariable("albumId") String albumId) {
        return albumPublicService.getById(albumId);
    }

    @Operation(summary = "Get several albums by ID",
            description = "Batch lookup for callers that already hold a list of album IDs, so a screen " +
                    "showing many albums costs one request rather than one per album. IDs that do not " +
                    "resolve to a publicly available album are omitted from the response rather than " +
                    "causing an error, so the result may be shorter than the request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The publicly available albums among those requested"),
            @ApiResponse(responseCode = "400", description = "The request body was empty or malformed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/get")
    public Mono<List<AlbumPublicStandaloneDto>> getAlbums(@RequestBody IdsRequest request) {
        return albumPublicService.getAllByIds(request.getIds());
    }

    @Operation(summary = "List tracks of an album", description = "Returns a paginated page of the public tracklist for the given album.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of tracks returned (possibly empty)"),
            @ApiResponse(responseCode = "404", description = "Album not found or not publicly available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{albumId}/tracks")
    public Mono<PaginationResponse<TrackPublicStandaloneDto>> getTracksByAlbum(
            @Parameter(description = "Base62-encoded short ID of the album.") @PathVariable("albumId") String albumId,
            @Parameter(description = "Zero-based offset of the first item to return.") @RequestParam(required = false, defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of items to return.") @RequestParam(required = false, defaultValue = "50") int limit) {
        return trackPublicService.getAllByAlbumId(albumId, offset, limit);
    }
}
