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
import org.ultra.rcrs.mediaservice.dto.AudioItemGroupBy;
import org.ultra.rcrs.mediaservice.service.AudioService;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media/audios")
@Tag(name = "Audio", description = "Read-only access to the transcoded audio variants stored for a track.")
public class AudioController {

    private final AudioService audioService;

    @Operation(
            summary = "List audio variants for a track",
            description = "Returns every stored audio asset for the given track, grouped by the logical audio guid "
                    + "(e.g. one guid per uploaded file, each with its available quality/transcoding variants). "
                    + "Returns an empty map when the track has no audio."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audio items grouped by guid returned successfully"),
            @ApiResponse(responseCode = "400", description = "trackId parameter is missing or malformed"),
            @ApiResponse(responseCode = "500", description = "Unexpected internal error")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<UUID, AudioItemGroupBy>> getAudiosByTrackId(
            @Parameter(description = "Short/public identifier of the track whose audio assets should be listed")
            @RequestParam String trackId) {
        return ResponseEntity.ok(audioService.getAudiosByTrackId(trackId));
    }
}
