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
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.mediaservice.dto.PresignedUrlResponse;
import org.ultra.rcrs.mediaservice.enums.Quality;
import org.ultra.rcrs.mediaservice.service.DownloadService;

import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "Download", description = "Generates presigned download URLs for stored audio assets.")
public class DownloadController {

    private final DownloadService downloadService;

    @Operation(
            summary = "Get a presigned download URL for an audio file",
            description = "Resolves a downloadable presigned URL either by exact audio asset id, or by track id + "
                    + "quality (LOW/MID/HIGH). Exactly one of audioId or trackId must be supplied; when trackId is "
                    + "used, quality is also required. Returns 400 if both are null, or if trackId is given without "
                    + "quality, and 404 if the referenced audio/track/quality variant cannot be found."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned download URL returned successfully"),
            @ApiResponse(responseCode = "400", description = "Neither audioId nor trackId was supplied, or trackId was supplied without quality"),
            @ApiResponse(responseCode = "404", description = "No audio asset matches the given audioId, or no matching quality variant exists for the track"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> downloadByAudioId(
            @Parameter(description = "Exact id of the audio asset to download; mutually exclusive alternative to trackId")
            @RequestParam(value = "audioId", required = false) UUID audioId,
            @Parameter(description = "Short/public identifier of the track to download audio for; requires quality to also be set")
            @RequestParam(value = "trackId", required = false) String trackId,
            @Parameter(description = "Desired audio quality variant (LOW, MID, HIGH); required when trackId is used")
            @RequestParam(value = "quality", required = false) Quality quality) {
        PresignedUrlResponse response;
        if (audioId == null && trackId == null) {
            throw new BadRequestException("one of params must not be null");
        } else if (trackId != null) {
            if (quality == null) {
                throw new BadRequestException("quality not be null");
            }
            response = downloadService.getByTrackId(trackId, quality);
        } else {
            response = downloadService.getByAudioId(audioId);
        }
        return ResponseEntity.ok(response);
    }
}
