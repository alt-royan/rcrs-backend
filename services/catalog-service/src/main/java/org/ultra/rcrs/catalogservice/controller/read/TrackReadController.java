package org.ultra.rcrs.catalogservice.controller.read;

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
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class TrackReadController {

    private final TrackCrudService trackCrudService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackMetadataAbstract>> getTrack(@PathVariable("trackId") String trackId, @PathVariable("published") boolean published) {
        return trackCrudService.getTrack(Url62.decode(trackId), published)
                .map(ResponseEntity::ok);
    }

}
