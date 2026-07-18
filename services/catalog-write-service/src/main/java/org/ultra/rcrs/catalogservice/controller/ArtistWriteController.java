package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.service.ArtistService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/artists")
public class ArtistWriteController {

    private final ArtistService artistService;

    @PostMapping
    public ResponseEntity<Void> createArtist(@RequestBody @Validated ArtistCreateRequest request) {
        artistService.createArtist(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{artistId}/hide")
    public ResponseEntity<Void> hideArtist(@PathVariable("artistId") String artistId) {
        artistService.hideArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@PathVariable("artistId") String artistId) {
        artistService.deleteArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }
}
