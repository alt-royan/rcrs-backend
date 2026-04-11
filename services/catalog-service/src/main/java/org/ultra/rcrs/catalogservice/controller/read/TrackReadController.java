package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.service.read.TrackReadService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class TrackReadController {

    private final TrackReadService trackReadService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackFullDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackReadService.getTrack(Url62.decode(trackId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

}
