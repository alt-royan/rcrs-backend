package org.ultra.rcrs.metadata.controller.publ;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.metadata.dto.ErrorResponse;
import org.ultra.rcrs.metadata.dto.TrackPublicViewDto;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Mono;

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
}
