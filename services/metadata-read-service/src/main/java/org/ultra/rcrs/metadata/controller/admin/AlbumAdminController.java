package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.AlbumAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.AlbumAdminViewDto;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/albums")
@CrossOrigin("*")
public class AlbumAdminController {

    private final AlbumAdminService albumAdminService;
    private final TrackAdminService trackAdminService;

    @GetMapping("/{albumId}")
    public Mono<AlbumAdminViewDto> getAlbum(@PathVariable("albumId") String albumId) {
        return albumAdminService.getById(albumId);
    }

    @GetMapping
    public Flux<AlbumAdminStandaloneDto> getAlbums(
            @RequestParam(required = false) EntityStatus availabilityStatus,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(required = false) AlbumType type,
            @RequestParam(required = false) Boolean explicit,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "50") int limit,
            @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumAdminService.getAll(availabilityStatus, lifecycleStatus, type, explicit, offset, limit, sort);
    }

    @GetMapping("/count")
    public Mono<Long> countAlbums(
            @RequestParam(required = false) EntityStatus availabilityStatus,
            @RequestParam(required = false) LifecycleStatus lifecycleStatus,
            @RequestParam(required = false) AlbumType type,
            @RequestParam(required = false) Boolean explicit) {
        return albumAdminService.count(availabilityStatus, lifecycleStatus, type, explicit);
    }

    @GetMapping("/{albumId}/tracks")
    public Flux<TrackAdminStandaloneDto> getTracksByAlbum(@PathVariable("albumId") String albumId) {
        return trackAdminService.getAllByAlbumId(albumId);
    }
}
