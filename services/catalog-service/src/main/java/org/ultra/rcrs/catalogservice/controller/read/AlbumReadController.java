package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumPage;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbum;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
import org.ultra.rcrs.catalogservice.service.TrackCrudService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class AlbumReadController {

    private final AlbumCrudService albumCrudService;
    private final TrackCrudService trackCrudService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumPage>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumCrudService.getAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<List<TrackInAlbum>>> getTracksForAlbum(@PathVariable("albumId") String albumId) {
        return trackCrudService.getTracksForAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }
}
