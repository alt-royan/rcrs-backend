package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.catalogservice.service.ArtistService;
import org.ultra.rcrs.enums.AlbumsOrder;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;

    private final AlbumService albumReadService;

    private final ArtistService artistRegisterService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistDto>> getArtist(@PathVariable("artistId") String artistId) {
        return artistService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums")
    public Mono<ResponseEntity<ItemListDto<AlbumSimplifyDto>>> getAlbumsForArtist(@PathVariable("artistId") String artistId,
                                                                                  @RequestParam(value = "order", required = false, defaultValue = "desc") AlbumsOrder order) {
        return albumReadService.getAlbumsForArtist_Main(Url62.decode(artistId), order)
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<ArtistDto>> registerNewArtist(@RequestBody @Validated ArtistRegisterDto artistRegisterDto) {
        return artistRegisterService.registerNewArtist(artistRegisterDto)
                .map(ResponseEntity::ok);
    }
}
