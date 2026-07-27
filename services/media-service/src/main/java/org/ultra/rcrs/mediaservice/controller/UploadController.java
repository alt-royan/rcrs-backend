package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.mediaservice.dto.*;
import org.ultra.rcrs.mediaservice.service.AudioService;
import org.ultra.rcrs.mediaservice.service.ImageUploadService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/media/upload")
public class UploadController {

    private final AudioService audioService;
    private final ImageUploadService imageUploadService;


    @PostMapping(value = "/audio/pre-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S3PresignUrlResponse> getPreSignUrl(@RequestBody @Validated PreloadFileRequest request) {
        return ResponseEntity.ok(audioService.getPreSignUrl(request));
    }

    @PostMapping(value = "/audio/get-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FileStatusResponse>> getAudioStatus(@RequestParam(value = "uids") List<String> uids) {
        if (uids == null) {
            uids = new ArrayList<>();
        }
        return ResponseEntity.ok(audioService.getAudioStatus(uids));
    }

    @PostMapping(value = "/image")
    public ResponseEntity<ImageResponse> uploadImage(@RequestBody ImageUploadRequest request) {
        return ResponseEntity.ok(imageUploadService.uploadImage(request.getImage()));
    }
}
