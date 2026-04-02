package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.service.TrackService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackService trackService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackService.getTrack(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{trackId}")
    public Mono<ResponseEntity<Void>> deleteTrack(@PathVariable("trackId") String trackId) {
        return trackService.deleteTrackById(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

}
