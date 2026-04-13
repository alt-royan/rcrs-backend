package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.service.read.ArtistReadService;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artists")
@CrossOrigin("*")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class ArtistReadController {

    private final ArtistReadService artistReadService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistDto>> getArtist(@PathVariable("artistId") String artistId) {
        return artistReadService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/")
    public Mono<ResponseEntity<ArtistDto>> searchArtists(@PathVariable("artistId") String artistId) {
        return artistReadService.getArtist(Url62.decode(artistId))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{artistId}/albums")
    public Mono<ResponseEntity<List<AlbumOfArtistDto>>> getAlbumsForArtist(@PathVariable("artistId") String artistId,
                                                                           @RequestParam(value = "direction", required = false) Sort.Direction direction,
                                                                           @RequestParam(value = "type", required = false) AlbumType type,
                                                                           @RequestParam(value = "role", required = false) ArtistRole role) {

        direction = direction == null ? Sort.Direction.DESC : direction;

        return artistReadService.getAlbumsForArtist(Url62.decode(artistId), List.of(EntityStatus.PUBLISHED), role, type, direction)
                .map(ResponseEntity::ok);
    }

}
