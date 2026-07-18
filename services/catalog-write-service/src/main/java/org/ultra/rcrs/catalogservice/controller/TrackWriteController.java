package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.StatusDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.service.TrackService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/tracks")
public class TrackWriteController {

    private final TrackService trackService;

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable("trackId") String trackId) {
        trackService.deleteTrack(Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{trackId}/status")
    public ResponseEntity<Void> updateTrackStatus(@RequestBody @Validated StatusDto statusDto, @PathVariable("trackId") String trackId) {
        trackService.updateLifecycleStatus(statusDto.getStatus(), Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }
    @PostMapping
    public ResponseEntity<Void> createTrack(@RequestBody @Validated TrackUploadRequest request) {
        trackService.createTrack(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{trackId}/hide")
    public ResponseEntity<Void> hideTrack(@PathVariable("trackId") String trackId) {
        trackService.hideTrack(Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

}
