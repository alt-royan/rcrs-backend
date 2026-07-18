package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistsToEntityRequest;
import org.ultra.rcrs.catalogservice.dto.request.StatusDto;
import org.ultra.rcrs.catalogservice.service.AlbumService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/metadata/albums")
public class AlbumWriteController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<Void> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        albumService.createAlbum(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{albumId}/artists")
    public ResponseEntity<Void> addArtistsToAlbum(@RequestBody @Validated ArtistsToEntityRequest request, @PathVariable("albumId") String albumId) {
        albumService.addAllArtistToAlbum(request.getArtists(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{albumId}/artists")
    public ResponseEntity<Void> deleteArtistsFromAlbum(@RequestBody @Validated ArtistsToEntityRequest request, @PathVariable("albumId") String albumId) {
        albumService.deleteAllArtistFromAlbum(request.getArtists(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{albumId}/status")
    public ResponseEntity<Void> updateAlbumStatus(@RequestBody @Validated StatusDto statusDto, @PathVariable("albumId") String albumId) {
        albumService.updateLifecycleStatus(statusDto.getStatus(), Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{albumId}/hide")
    public ResponseEntity<Void> hideAlbum(@PathVariable("albumId") String albumId) {
        albumService.hideAlbum(Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<Void> deleteAlbum(@PathVariable("albumId") String albumId) {
        albumService.deleteAlbum(Url62.decode(albumId));
        return ResponseEntity.ok().build();
    }
}
