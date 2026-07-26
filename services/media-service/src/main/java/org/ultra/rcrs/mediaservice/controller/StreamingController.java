package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.PresignedUrlResponse;
import org.ultra.rcrs.mediaservice.service.StreamingService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping(value = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> streamTrack(@RequestParam("trackId") String trackId,
                                                            @RequestParam("bitrate") String bitrate) {
        return ResponseEntity.ok(streamingService.streamTrack(trackId, bitrate));
    }

}
