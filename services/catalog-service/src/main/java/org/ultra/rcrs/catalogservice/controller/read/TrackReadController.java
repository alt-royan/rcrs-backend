package org.ultra.rcrs.catalogservice.controller.read;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackStandaloneDto;
import org.ultra.rcrs.catalogservice.service.read.TrackReadService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tracks")
@CrossOrigin("*")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class TrackReadController {

    private final TrackReadService trackReadService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackFullDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackReadService.getTrack(Url62.decode(trackId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/get")
    public Mono<ResponseEntity<List<TrackStandaloneDto>>> getTracks(@RequestBody @Validated @NotNull List<String> ids) {
        return trackReadService.getTracks(ids.stream().map(Url62::decode).toList(), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

}
