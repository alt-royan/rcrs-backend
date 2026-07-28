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
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.IdsRequest;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackPublicViewDto;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Track", description = "Public storefront endpoints for browsing publicly available tracks.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/catalog/tracks")
public class TrackPublicController {

    private final TrackPublicService trackPublicService;

    @Operation(summary = "Get a track by ID", description = "Returns the public detail view of a single track.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Track found"),
            @ApiResponse(responseCode = "404", description = "Track not found or not publicly available",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{trackId}")
    public Mono<TrackPublicViewDto> getTrack(
            @Parameter(description = "Base62-encoded short ID of the track.") @PathVariable("trackId") String trackId) {
        return trackPublicService.getById(trackId);
    }

    @Operation(summary = "Get several tracks by ID",
            description = "Batch lookup for callers that already hold a list of track IDs, so a screen " +
                    "showing many tracks costs one request rather than one per track. IDs that do not " +
                    "resolve to a publicly available track are omitted from the response rather than " +
                    "causing an error, so the result may be shorter than the request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The publicly available tracks among those requested"),
            @ApiResponse(responseCode = "400", description = "The request body was empty or malformed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/get")
    public Mono<List<TrackPublicStandaloneDto>> getTracks(@RequestBody IdsRequest request) {
        return trackPublicService.getAllByIds(request.getIds());
    }
}
