package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.full.FullAlbumMetadata;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class AlbumWriteController {

    private final AlbumCrudService albumCrudService;

    @DeleteMapping("/{albumId}")
    public Mono<ResponseEntity<Void>> deleteAlbum(@PathVariable("albumId") String albumId) {
        return albumCrudService.deleteAlbumCascadeById(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }

    @PostMapping
    public Mono<ResponseEntity<FullAlbumMetadata>> createAlbum(@RequestBody @Validated AlbumCreateRequest request) {
        return albumCrudService.createAlbum(request)
                .map(ResponseEntity::ok);
    }
}
