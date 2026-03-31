package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.service.AlbumReadService;
import org.ultra.rcrs.catalogservice.service.ArtistReadService;
import org.ultra.rcrs.catalogservice.service.ArtistRegisterService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistReadService artistReadService;

    private final AlbumReadService albumReadService;

    private final ArtistRegisterService artistRegisterService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistDto>> getArtist(@PathVariable("artistId") String artistId) {
        return artistReadService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums")
    public Mono<ResponseEntity<ItemListDto<AlbumSimplifyDto>>> getAlbumsForArtist(@PathVariable("artistId") String artistId) {
        return albumReadService.getAlbumsForArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }


    @PostMapping
    public Mono<ResponseEntity<ArtistDto>> registerNewArtist(@RequestBody @Validated ArtistRegisterDto artistRegisterDto) {
        return artistRegisterService.registerNewArtist(artistRegisterDto)
                .map(ResponseEntity::ok);
    }
}
