package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.TrackMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.request.TrackCreateRequest;
import org.ultra.rcrs.catalogservice.service.TrackCrudService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class TrackWriteController {

    private final TrackCrudService trackCrudService;

    @DeleteMapping("/{trackId}")
    public Mono<ResponseEntity<Void>> deleteTrack(@PathVariable("trackId") String trackId) {
        return trackCrudService.deleteTrackById(Url62.decode(trackId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<TrackMetadataAbstract>> createTrack(@RequestBody @Validated TrackCreateRequest request) {
        return trackCrudService.createTrack(request)
                .map(ResponseEntity::ok);
    }

}
