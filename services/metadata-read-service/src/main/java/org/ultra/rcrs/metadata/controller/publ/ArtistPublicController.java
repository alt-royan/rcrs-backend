package org.ultra.rcrs.metadata.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.ArtistPublicViewDto;
import org.ultra.rcrs.metadata.service.publ.ArtistPublicService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/artists")
public class ArtistPublicController {

    private final ArtistPublicService artistPublicService;

    @GetMapping("/{artistId}")
    public Mono<ArtistPublicViewDto> getArtist(@PathVariable("artistId") String artistId) {
        return artistPublicService.getById(artistId);
    }
}
