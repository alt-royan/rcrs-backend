package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.service.AlbumReadService;
import org.ultra.rcrs.catalogservice.service.ArtistReadService;
import org.ultra.rcrs.catalogservice.validation.annotation.ValidBase62UUID;
import org.ultra.rcrs.utils.Base62Utils;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistReadService artistReadService;

    private final AlbumReadService albumReadService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistDto>> getArtist(@PathVariable("artistId") @ValidBase62UUID String artistId) {
        return artistReadService.getArtist(Base62Utils.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums")
    public Mono<ResponseEntity<ItemListDto<AlbumSimplifyDto>>> getAlbumsForArtist(@PathVariable("artistId") @ValidBase62UUID String artistId) {
        return albumReadService.getAlbumsForArtist(Base62Utils.decode(artistId))
                .map(ResponseEntity::ok);
    }
}
