package org.ultra.rcrs.catalogservice.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistPublicViewDto;
import org.ultra.rcrs.catalogservice.service.publ.ArtistPublicService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
public class ArtistPublicController {

    private final ArtistPublicService artistPublicService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistPublicViewDto>> getArtist(@PathVariable("artistId") String artistId) {
        return artistPublicService.getById(artistId)
                .map(ResponseEntity::ok);
    }
}
