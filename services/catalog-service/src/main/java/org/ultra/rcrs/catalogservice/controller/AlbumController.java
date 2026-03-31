package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.service.AlbumCreateService;
import org.ultra.rcrs.catalogservice.service.AlbumReadService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumReadService albumReadService;
    private final AlbumCreateService albumCreateService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumReadService.getAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<Album>> createAlbum(@RequestBody @Validated AlbumCreateDto dto) {
        return albumCreateService.createAlbum(dto)
                .map(ResponseEntity::ok);
    }
}
