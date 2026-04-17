package org.ultra.rcrs.catalogservice.controller.read;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.service.read.AlbumReadService;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/albums")
@CrossOrigin("*")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class AlbumReadController {

    private final AlbumReadService albumReadService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumFullDto>> getAlbum(@PathVariable("albumId") String albumId) {
        return albumReadService.getAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @PostMapping("/get")
    public Mono<ResponseEntity<List<AlbumStandaloneDto>>> getAlbums(@RequestBody @Validated @NotNull List<String> ids) {
        return albumReadService.getAlbums(ids.stream().map(Url62::decode).toList(), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<List<TrackInAlbumDto>>> getTracksInAlbum(@PathVariable("albumId") String albumId) {
        return albumReadService.getTracksInAlbum(Url62.decode(albumId), List.of(EntityStatus.PUBLISHED))
                .map(ResponseEntity::ok);
    }
}
