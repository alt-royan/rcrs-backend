package org.ultra.rcrs.catalogservice.controller.read;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.full.FullAlbumMetadata;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleTrackMetadata;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.read.enabled", havingValue = "true")
public class AlbumReadController {

    private final AlbumCrudService albumCrudService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<FullAlbumMetadata>> getAlbum(@PathVariable("albumId") String albumId, @PathVariable("published") boolean published) {
        return albumCrudService.getAlbum(Url62.decode(albumId), published)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/{albumId}/tracks")
    public Mono<ResponseEntity<List<SimpleTrackMetadata>>> getTracksForAlbum(@PathVariable("albumId") String albumId) {
        return albumCrudService.getTracksForAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }
}
