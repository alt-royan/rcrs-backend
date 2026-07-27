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
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.metadata.dto.AlbumPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistPublicViewDto;
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.PaginationResponse;
import org.ultra.rcrs.metadata.service.publ.AlbumPublicService;
import org.ultra.rcrs.metadata.service.publ.ArtistPublicService;
import reactor.core.publisher.Mono;

@Tag(name = "Artist", description = "Public storefront endpoints for browsing publicly available artists and their albums.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/artists")
public class ArtistPublicController {

    private final AlbumPublicService albumPublicService;
    private final ArtistPublicService artistPublicService;

    @Operation(summary = "Get an artist by ID", description = "Returns the public detail view of a single artist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artist found"),
            @ApiResponse(responseCode = "404", description = "Artist not found or not publicly available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{artistId}")
    public Mono<ArtistPublicViewDto> getArtist(
            @Parameter(description = "Base62-encoded short ID of the artist.") @PathVariable("artistId") String artistId) {
        return artistPublicService.getById(artistId);
    }

    @Operation(summary = "List albums of an artist", description = "Returns a paginated page of the public albums credited to the given artist, optionally filtered by album type and sorted by release date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of albums returned (possibly empty)"),
            @ApiResponse(responseCode = "404", description = "Artist not found or not publicly available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{artistId}/albums")
    public Mono<PaginationResponse<AlbumPublicStandaloneDto>> getAlbumsByArtist(
            @Parameter(description = "Base62-encoded short ID of the artist.") @PathVariable("artistId") String artistId,
            @Parameter(description = "Optional album type filter (e.g. ALBUM, SINGLE, EP, COMPILATION).") @RequestParam(required = false) AlbumType type,
            @Parameter(description = "Sort direction by release date: \"asc\" or \"desc\".") @RequestParam(required = false, defaultValue = "asc") String sort,
            @Parameter(description = "Zero-based offset of the first item to return.") @RequestParam(required = false, defaultValue = "0") int offset,
            @Parameter(description = "Maximum number of items to return.") @RequestParam(required = false, defaultValue = "50") int limit) {
        return albumPublicService.getAllByArtistId(artistId, type, sort, offset, limit);
    }
}
