package org.ultra.rcrs.mediaservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.PresignedUrlResponse;
import org.ultra.rcrs.mediaservice.enums.Quality;
import org.ultra.rcrs.mediaservice.service.StreamingService;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "Streaming", description = "Generates presigned streaming URLs for tracks at a requested audio quality.")
public class StreamingController {

    private final StreamingService streamingService;

    @Operation(
            summary = "Get a presigned streaming URL for a track",
            description = "Resolves a short-lived presigned URL that can be used to stream the requested quality "
                    + "variant of the given track's audio directly from storage."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned streaming URL returned successfully"),
            @ApiResponse(responseCode = "400", description = "trackId or quality parameter is missing or malformed"),
            @ApiResponse(responseCode = "404", description = "Track not found, or no audio variant exists for the requested quality"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> streamTrack(
            @Parameter(description = "Short/public identifier of the track to stream")
            @RequestParam("trackId") String trackId,
            @Parameter(description = "Desired audio quality variant to stream (LOW, MID, HIGH)")
            @RequestParam("quality") Quality quality) {
        return ResponseEntity.ok(streamingService.streamTrack(trackId, quality));
    }

}
