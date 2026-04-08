package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.service.ImageService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/image", consumes = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> uploadImage(@RequestBody String dataUrl) throws IOException {
        return ResponseEntity.ok(imageService.uploadImage(dataUrl));
    }
}
