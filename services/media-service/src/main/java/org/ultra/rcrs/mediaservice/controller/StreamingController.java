package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.StreamingUrlsDto;
import org.ultra.rcrs.mediaservice.service.StreamingService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping(value = "/track/{trackId}/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingUrlsDto> streamTrack(@PathVariable("trackId") String trackId) {
        return ResponseEntity.ok(streamingService.streamTrack(trackId));
    }

    @PostMapping(value = "/audio/{audioId}/stream/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingUrlsDto> streamAudio(@PathVariable("audioId") String audioId) {
        UUID uuid = UUID.fromString(audioId);
        return ResponseEntity.ok(streamingService.streamAudio(uuid));
    }

}
