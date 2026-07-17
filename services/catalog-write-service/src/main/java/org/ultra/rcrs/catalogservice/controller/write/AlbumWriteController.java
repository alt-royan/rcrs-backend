package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/albums")
public class AlbumWriteController {

    private final AlbumWriteService albumWriteService;

    @PostMapping
    public ResponseEntity<IdResponse> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        return ResponseEntity.ok(albumWriteService.createAlbum(request));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable("albumId") String albumId) {
        albumWriteService.deleteAlbum(Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }
}
