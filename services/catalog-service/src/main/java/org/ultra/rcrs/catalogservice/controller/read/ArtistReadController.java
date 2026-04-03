package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.catalogservice.service.ArtistService;
import org.ultra.rcrs.enums.AlbumsOrder;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class ArtistReadController {

    private final ArtistService artistService;

    private final AlbumService albumReadService;

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
}
