package org.ultra.rcrs.metadata.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.metadata.dto.AlbumPublicViewDto;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.service.publ.AlbumPublicService;
import org.ultra.rcrs.metadata.service.publ.TrackPublicService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog/albums")
public class AlbumPublicController {

    private final AlbumPublicService albumPublicService;
    private final TrackPublicService trackPublicService;

    @GetMapping("/{albumId}")
    public Mono<AlbumPublicViewDto> getAlbum(@PathVariable("albumId") String albumId) {
        return albumPublicService.getById(albumId);
    }

    @GetMapping("/{albumId}/tracks")
    public Flux<TrackPublicStandaloneDto> getTracksByAlbum(@PathVariable("albumId") String albumId) {
        return trackPublicService.getAllByAlbumId(albumId);
    }
}
