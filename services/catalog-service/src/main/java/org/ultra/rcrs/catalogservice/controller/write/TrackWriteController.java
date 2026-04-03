package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackCreateRequest;
import org.ultra.rcrs.catalogservice.service.TrackService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class TrackWriteController {

    private final TrackService trackService;

    @DeleteMapping("/{trackId}")
    public Mono<ResponseEntity<Void>> deleteTrack(@PathVariable("trackId") String trackId) {
        return trackService.deleteTrackById(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<TrackDto>> createTrack(@RequestBody @Validated TrackCreateRequest request) {
        return trackService.createTrack(request)
                .map(ResponseEntity::ok);
    }

}
