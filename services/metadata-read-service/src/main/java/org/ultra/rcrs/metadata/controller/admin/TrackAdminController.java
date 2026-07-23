package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Flux;
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

    @GetMapping
    public Flux<TrackAdminStandaloneDto> getTracks(
            @RequestParam(required = false) EntityStatus availabilityStatus,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(required = false) String albumId,
            @RequestParam(required = false) Boolean explicit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "50") int limit,
            @RequestParam(required = false, defaultValue = "asc") String sort) {
        return trackAdminService.getAll(availabilityStatus, lifecycleStatus, albumId, explicit, offset, limit, sort);
    }

    @GetMapping("/count")
    public Mono<Long> countTracks(
            @RequestParam(required = false) EntityStatus availabilityStatus,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(required = false) String albumId,
            @RequestParam(required = false) Boolean explicit) {
        return trackAdminService.count(availabilityStatus, lifecycleStatus, albumId, explicit);
    }
}
