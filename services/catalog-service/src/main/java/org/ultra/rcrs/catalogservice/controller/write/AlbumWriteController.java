package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.service.AlbumCrudService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class AlbumWriteController {

    private final AlbumCrudService albumCrudService;

    @PostMapping
    public Mono<ResponseEntity<Void>> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        return albumCrudService.createAlbum(request)
                .map(ResponseEntity::ok);
    }
}
