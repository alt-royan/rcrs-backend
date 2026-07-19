package org.ultra.rcrs.metadata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.dto.request.ArtistsToEntityRequest;
import org.ultra.rcrs.metadata.dto.StatusDto;
import org.ultra.rcrs.metadata.dto.response.CreateResponse;
import org.ultra.rcrs.metadata.service.AlbumService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/albums")
public class AlbumWriteController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<CreateResponse> createAlbum(@RequestBody @Validated AlbumUploadRequest request) {
        var res = albumService.createAlbum(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateResponse(res));
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

    @PutMapping("/{albumId}/status")
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
        albumService.markAlbumDelete(Url62.decode(albumId));
        return ResponseEntity.noContent().build();
    }
}
