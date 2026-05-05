package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.ImageResponse;
import org.ultra.rcrs.mediaservice.dto.ImageUploadRequest;
import org.ultra.rcrs.mediaservice.service.ImageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/upload/files")
public class FileController {

    private final ImageService imageService;

    @PostMapping(value = "/image")
    public ResponseEntity<ImageResponse> uploadImage(@RequestBody ImageUploadRequest request) {
        return ResponseEntity.ok(imageService.uploadImage(request.getImage()));
    }
}
