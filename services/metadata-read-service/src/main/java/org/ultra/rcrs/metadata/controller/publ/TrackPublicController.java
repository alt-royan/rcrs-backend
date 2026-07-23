package org.ultra.rcrs.metadata.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.metadata.dto.TrackPublicViewDto;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracks")
public class TrackPublicController {

    private final TrackPublicService trackPublicService;

    @GetMapping("/{trackId}")
    public Mono<TrackPublicViewDto> getTrack(@PathVariable("trackId") String trackId) {
        return trackPublicService.getById(trackId);
    }
}
