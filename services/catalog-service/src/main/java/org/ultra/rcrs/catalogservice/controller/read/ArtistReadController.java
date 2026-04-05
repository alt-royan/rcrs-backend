package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandalone;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistPage;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
import org.ultra.rcrs.catalogservice.service.ArtistCrudService;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class ArtistReadController {

    private final ArtistCrudService artistCrudService;
    private final AlbumCrudService albumCrudService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistPage>> getArtist(@PathVariable("artistId") String artistId) {
        return artistCrudService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums?role=main_artist?type=single")
    public Mono<ResponseEntity<List<AlbumStandalone>>> getAlbumsForArtist(@PathVariable("artistId") String artistId,
                                                                          @RequestParam(value = "direction", required = false) Sort.Direction direction,
                                                                          @RequestParam(value = "types", required = false) AlbumType[] types,
                                                                          @RequestParam(value = "roles", required = false) ArtistRole[] roles) {

        roles = roles.length == 0 ? ArtistRole.values() : roles;
        types = types.length == 0 ? AlbumType.values() : types;
        direction = direction == null ? Sort.Direction.DESC : direction;

        return albumCrudService.getAlbumsForArtist(Url62.decode(artistId), List.of(EntityStatus.PUBLISHED), roles, types, direction)
                .map(ResponseEntity::ok);
    }

}
