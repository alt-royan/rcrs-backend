package org.ultra.rcrs.mediaservice.controller;

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
import org.ultra.rcrs.mediaservice.service.DownloadService;

import java.util.UUID;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Tag(name = "Download", description = "Generates presigned download URLs for stored audio assets.")
public class DownloadController {

    private final DownloadService downloadService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned download URL returned successfully"),
            @ApiResponse(responseCode = "400", description = "Neither audioId nor trackId was supplied, or trackId was supplied without quality"),
            @ApiResponse(responseCode = "404", description = "No audio asset matches the given audioId, or no matching quality variant exists for the track"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @GetMapping(value = "/download", params = {"audioId"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> downloadByAudioId(
            @Parameter(description = "Exact id of the audio asset to download; mutually exclusive alternative to trackId")
            @RequestParam(value = "audioId") UUID audioId) {
        PresignedUrlResponse response = downloadService.getByAudioId(audioId);
        return ResponseEntity.ok(response);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned download URL returned successfully"),
            @ApiResponse(responseCode = "400", description = "Neither audioId nor trackId was supplied, or trackId was supplied without quality"),
            @ApiResponse(responseCode = "404", description = "No audio asset matches the given audioId, or no matching quality variant exists for the track"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @GetMapping(value = "/download", params = {"trackId", "quality"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> downloadByTrackId(
            @Parameter(description = "Short/public identifier of the track to download audio for; requires quality to also be set")
            @RequestParam(value = "trackId") String trackId,
            @Parameter(description = "Desired audio quality variant (LOW, MID, HIGH); required when trackId is used")
            @RequestParam(value = "quality") Quality quality) {
        PresignedUrlResponse response = downloadService.getByTrackId(trackId, quality);
        return ResponseEntity.ok(response);
    }
}
