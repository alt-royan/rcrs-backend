package org.ultra.rcrs.catalogservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.service.ArtistWriteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/artists")
public class ArtistWriteController {

    private final ArtistWriteService artistWriteService;

    @PostMapping
    public ResponseEntity<Void> createNewArtist(@RequestBody @Validated ArtistCreateRequest request) {
        artistWriteService.createArtist(request);
        return ResponseEntity.ok().build();
    }
}
