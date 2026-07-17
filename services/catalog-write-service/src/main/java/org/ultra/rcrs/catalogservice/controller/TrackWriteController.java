package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.StatusDto;
import org.ultra.rcrs.catalogservice.service.TrackWriteService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/tracks")
public class TrackWriteController {

    private final TrackWriteService trackWriteService;

    @DeleteMapping("/{trackId}")
    public ResponseEntity<Void> deleteTrack(@PathVariable("trackId") String trackId) {
        trackWriteService.deleteTrack(Url62.decode(trackId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{trackId}/status")
    public ResponseEntity<Void> updateTrackStatus(@RequestBody @Validated StatusDto statusDto, @PathVariable("trackId") String trackId) {
        trackWriteService.updateStatus(Url62.decode(trackId), statusDto.getStatus());
        return ResponseEntity.ok().build();
    }
}
