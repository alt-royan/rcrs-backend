package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.TrackAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/tracks")
@CrossOrigin("*")
public class TrackAdminController {

    private final TrackAdminService trackAdminService;

    @GetMapping("/{trackId}")
    public Mono<TrackAdminViewDto> getTrack(@PathVariable("trackId") String trackId) {
        return trackAdminService.getById(trackId);
    }
}
