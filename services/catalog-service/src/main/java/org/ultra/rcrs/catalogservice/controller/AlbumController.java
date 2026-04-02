package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumService albumReadService;
    private final AlbumService albumService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumReadService.getAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<ItemListDto<TrackSimplifyDto>>> getTracksForAlbum(@PathVariable("albumId") String albumId) {
        return albumReadService.getTracksForAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Album>> createAlbum(@RequestBody @Validated AlbumCreateDto dto) {
        return albumService.createAlbum(dto)
                .map(ResponseEntity::ok);
    }
}
