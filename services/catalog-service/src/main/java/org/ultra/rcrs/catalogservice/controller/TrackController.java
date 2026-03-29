package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.service.TrackReadService;
import org.ultra.rcrs.catalogservice.validation.annotation.ValidBase62UUID;
import org.ultra.rcrs.utils.Base62Utils;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
public class TrackController {

    private final TrackReadService trackReadService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackDto>> getTrack(@PathVariable("trackId") @ValidBase62UUID String trackId) {
        return trackReadService.getTrack(Base62Utils.decode(trackId))
                .map(ResponseEntity::ok);
    }
}
