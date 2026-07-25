package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.DownloadFileResponse;
import org.ultra.rcrs.mediaservice.service.DownloadService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/downloads")
public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping(value = "/audio/{audioId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DownloadFileResponse> downloadByAudioId(@PathVariable("audioId") UUID audioId) {
        return ResponseEntity.ok(downloadService.getByAudioId(audioId));
    }

    @GetMapping(value = "/track/{trackId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DownloadFileResponse> downloadByTrackId(@PathVariable("trackId") String trackId) {
        return ResponseEntity.ok(downloadService.getByTrackId(trackId));
    }
}
