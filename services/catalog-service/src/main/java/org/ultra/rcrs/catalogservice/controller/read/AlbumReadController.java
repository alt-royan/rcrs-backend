package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class AlbumReadController {

    private final AlbumService albumService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumService.getAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<ItemListDto<TrackSimplifyDto>>> getTracksForAlbum(@PathVariable("albumId") String albumId) {
        return albumService.getTracksForAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }
}
