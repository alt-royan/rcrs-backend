package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleAlbumMetadata;
import org.ultra.rcrs.catalogservice.service.ArtistCrudService;
import org.ultra.rcrs.enums.Order;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class ArtistReadController {

    private final ArtistCrudService artistCrudService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistMetadataAbstract>> getArtist(@PathVariable("artistId") String artistId) {
        return artistCrudService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums")
    public Mono<ResponseEntity<List<SimpleAlbumMetadata>>> getAlbumsForArtist(@PathVariable("artistId") String artistId,
                                                                              @RequestParam(value = "order", required = false, defaultValue = "desc") Order order,
                                                                              @RequestParam(value = "role", required = false, defaultValue = "MAIN_ARTIST") ArtistRole role) {
        return artistCrudService.getAlbumsForArtist(Url62.decode(artistId), order, role)
                .map(ResponseEntity::ok);
    }

}
