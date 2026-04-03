package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.service.TrackService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class TrackReadController {

    private final TrackService trackService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackService.getTrack(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

}
