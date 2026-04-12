package org.ultra.rcrs.uploadservice.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.uploadservice.dto.AlbumUploadRequest;
import org.ultra.rcrs.uploadservice.dto.ArtistCreateRequest;
import org.ultra.rcrs.uploadservice.dto.ImageUploadRequest;
import org.ultra.rcrs.uploadservice.dto.PreloadFileRequest;
import org.ultra.rcrs.uploadservice.service.UploadService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UploadController {

    private final UploadService service;

    @PostMapping(value = "/upload/album", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadAlbum(@RequestBody @Validated @NotNull AlbumUploadRequest request) {
        return service.uploadAlbum(request);
    }

    @PostMapping(value = "/upload/artist", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadArtist(@RequestBody @Validated @NotNull ArtistCreateRequest request) {
        return service.uploadArtist(request);
    }

    @PostMapping(value = "/preload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> preload(@RequestBody @Validated @NotNull PreloadFileRequest request) {
        return service.preSigned(request);
    }

    @PostMapping(value = "/upload/image", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> uploadImage(@RequestBody @Validated @NotNull ImageUploadRequest request) {
        return service.uploadImage(request);
    }

    @PostMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getFilesStatus(@RequestBody @Validated List<String> uids) {
        return service.getFilesStatus(uids);
    }

}
