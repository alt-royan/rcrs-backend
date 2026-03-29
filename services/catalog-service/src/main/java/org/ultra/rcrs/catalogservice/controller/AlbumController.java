package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.service.AlbumReadService;
import org.ultra.rcrs.catalogservice.validation.annotation.ValidBase62UUID;
import org.ultra.rcrs.utils.Base62Utils;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/albums")
public class AlbumController {

    private final AlbumReadService albumReadService;

    @GetMapping("/{albumId}")
    public Mono<ResponseEntity<AlbumDto>> getAlbum(@PathVariable("albumId") @ValidBase62UUID String albumId) {
        return albumReadService.getAlbum(Base62Utils.decode(albumId))
                .map(ResponseEntity::ok);
    }
}
