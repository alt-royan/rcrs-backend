package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.mediaservice.dto.FileStatusResponse;
import org.ultra.rcrs.mediaservice.dto.PreloadFileRequest;
import org.ultra.rcrs.mediaservice.dto.S3PresignUrlResponse;
import org.ultra.rcrs.mediaservice.service.FileService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/pre-sign", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<S3PresignUrlResponse> getPreSignUrl(@RequestBody @Validated PreloadFileRequest request) {
        return ResponseEntity.ok(fileService.getPreSignUrl(request));
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, FileStatusResponse>> getFilesStatus(@RequestParam(value = "uids") List<String> uids) {
        if (uids == null) {
            uids = new ArrayList<>();
        }
        return ResponseEntity.ok(fileService.getFilesStatus(uids));
    }
}
