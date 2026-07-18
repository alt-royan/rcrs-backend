package org.ultra.rcrs.catalogservice.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.AlbumPublicViewDto;
import org.ultra.rcrs.catalogservice.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.catalogservice.service.publ.AlbumPublicService;
import org.ultra.rcrs.catalogservice.service.publ.TrackPublicService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumPublicController {

    private final AlbumPublicService albumPublicService;
    private final TrackPublicService trackPublicService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumPublicViewDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumPublicService.getById(albumId)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Flux<ResponseEntity<TrackPublicStandaloneDto>> getTracksByAlbum(@PathVariable("albumId") String albumId) {
        return trackPublicService.getByAlbumId(albumId)
                .map(ResponseEntity::ok);
    }
}
