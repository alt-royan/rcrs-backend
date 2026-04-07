package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
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

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumFullDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumCrudService.getAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<List<TrackInAlbumDto>>> getTracksInAlbum(@PathVariable("albumId") String albumId) {
        return albumCrudService.getTracksInAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }
}
