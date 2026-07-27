package org.ultra.rcrs.mediaservice.controller;

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
public class DownloadController {

    private final DownloadService downloadService;

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PresignedUrlResponse> downloadByAudioId(@RequestParam(value = "audioId", required = false) UUID audioId,
                                                                  @RequestParam(value = "trackId", required = false) String trackId,
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
