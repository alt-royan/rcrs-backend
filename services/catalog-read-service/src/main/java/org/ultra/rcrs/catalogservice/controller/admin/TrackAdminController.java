package org.ultra.rcrs.catalogservice.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.TrackAdminViewDto;
import org.ultra.rcrs.catalogservice.service.admin.TrackAdminService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/tracks")
public class TrackAdminController {

    private final TrackAdminService trackAdminService;

    @GetMapping("/{trackId}")
    public Mono<ResponseEntity<TrackAdminViewDto>> getTrack(@PathVariable("trackId") String trackId) {
        return trackAdminService.getById(trackId)
                .map(ResponseEntity::ok);
    }
}
