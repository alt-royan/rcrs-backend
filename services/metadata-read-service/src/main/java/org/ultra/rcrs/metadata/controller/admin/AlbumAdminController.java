package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.AlbumAdminViewDto;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.TrackAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/albums")
public class AlbumAdminController {

    private final AlbumAdminService albumAdminService;
    private final TrackAdminService trackAdminService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumAdminViewDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumAdminService.getById(albumId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Flux<ResponseEntity<TrackAdminStandaloneDto>> getTracksByAlbum(@PathVariable("albumId") String albumId) {
        return trackAdminService.getByAlbumId(albumId)
                .map(ResponseEntity::ok);
    }
}
