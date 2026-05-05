package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/albums")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class AlbumWriteController {

    private final AlbumWriteService albumWriteService;

    @PostMapping
    public Mono<ResponseEntity<IdResponse>> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        return albumWriteService.createAlbum(request)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{albumId}")
    public Mono<ResponseEntity<Void>> deleteTrack(@PathVariable("albumId") String albumId) {
        return albumWriteService.deleteAlbum(Url62.decode(albumId))
                .map(ResponseEntity::ok);
    }
}
