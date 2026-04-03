package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class AlbumWriteController {

    private final AlbumService albumService;

    @DeleteMapping("/{albumId}")
    public Mono<ResponseEntity<Void>> deleteAlbum(@PathVariable("albumId") String albumId) {
        return albumService.deleteAlbumCascadeById(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<AlbumDto>> createAlbum(@RequestBody @Validated AlbumCreateRequest request) {
        return albumService.createAlbum(request)
                .map(ResponseEntity::ok);
    }
}
