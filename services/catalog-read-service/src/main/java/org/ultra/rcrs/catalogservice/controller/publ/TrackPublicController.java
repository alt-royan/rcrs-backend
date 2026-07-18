package org.ultra.rcrs.catalogservice.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.TrackPublicViewDto;
import org.ultra.rcrs.catalogservice.service.publ.TrackPublicService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tracks")
public class TrackPublicController {

    private final TrackPublicService trackPublicService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackPublicViewDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackPublicService.getById(trackId)
                .map(ResponseEntity::ok);
    }
}
