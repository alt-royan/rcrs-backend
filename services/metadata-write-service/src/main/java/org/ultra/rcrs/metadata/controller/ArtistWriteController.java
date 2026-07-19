package org.ultra.rcrs.metadata.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.dto.response.CreateResponse;
import org.ultra.rcrs.metadata.service.ArtistService;
import org.ultra.rcrs.utils.Url62;

@RestController
@RequiredArgsConstructor
@RequestMapping("/artists")
public class ArtistWriteController {

    private final ArtistService artistService;

    @PostMapping
    public ResponseEntity<CreateResponse> createArtist(@RequestBody @Validated ArtistCreateRequest request) {
        var res = artistService.createArtist(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new CreateResponse(res));
    }

    @PutMapping("/{artistId}/hide")
    public ResponseEntity<Void> hideArtist(@PathVariable("artistId") String artistId) {
        artistService.hideArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{artistId}/active")
    public ResponseEntity<Void> activeArtist(@PathVariable("artistId") String artistId) {
        artistService.activeArtist(Url62.decode(artistId));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{artistId}")
    public ResponseEntity<Void> deleteArtist(@PathVariable("artistId") String artistId) {
        artistService.markArtistDelete(Url62.decode(artistId));
        return ResponseEntity.noContent().build();
    }
}
