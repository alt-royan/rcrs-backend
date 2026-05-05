package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.mediaservice.dto.FileStatusResponse;
import org.ultra.rcrs.mediaservice.dto.PreloadFileRequest;
import org.ultra.rcrs.mediaservice.dto.S3PresignUrlResponse;
import org.ultra.rcrs.mediaservice.service.AudioService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload/audio")
public class AudioController {

    private final AudioService audioService;

    @PostMapping(value = "/pre-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S3PresignUrlResponse> getPreSignUrl(@RequestBody @Validated PreloadFileRequest request) {
        return ResponseEntity.ok(audioService.getPreSignUrl(request));
    }

    @PostMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileStatusResponse>> getAudioStatus(@RequestParam(value = "uids") List<String> uids) {
        if (uids == null) {
            uids = new ArrayList<>();
        }
        return ResponseEntity.ok(audioService.getAudioStatus(uids));
    }
}
