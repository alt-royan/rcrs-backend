package org.ultra.rcrs.metadata.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.AlbumPublicViewDto;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.service.publ.AlbumPublicService;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums")
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
