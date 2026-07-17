package org.ultra.rcrs.catalogservice.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackStandaloneDto;
import org.ultra.rcrs.catalogservice.service.TrackReadService;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/tracks")
public class TrackReadController {

    private final TrackReadService trackReadService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackFullDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackReadService.getTrack(Url62.decode(trackId), List.of(LifecycleStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/get")
    public Mono<ResponseEntity<List<TrackStandaloneDto>>> getTracks(@RequestBody @Validated @NotNull List<String> ids) {
        return trackReadService.getTracks(ids.stream().map(Url62::decode).toList(), List.of(LifecycleStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }
}
