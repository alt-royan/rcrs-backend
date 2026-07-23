package org.ultra.rcrs.mediaservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.mediaservice.dto.StreamingRequest;
import org.ultra.rcrs.mediaservice.dto.StreamingUrlsDto;
import org.ultra.rcrs.mediaservice.service.StreamingService;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class StreamingController {

    private final StreamingService streamingService;

    @PostMapping(value = "/stream", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingUrlsDto> stream(@RequestBody @Validated StreamingRequest request) {
        return ResponseEntity.ok(streamingService.stream(request));
    }

}
