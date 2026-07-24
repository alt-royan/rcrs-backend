package org.ultra.rcrs.metadata.controller.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.metadata.dto.AlbumPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistPublicViewDto;
import org.ultra.rcrs.metadata.service.publ.AlbumPublicService;
import org.ultra.rcrs.metadata.service.publ.ArtistPublicService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/catalog/artists")
public class ArtistPublicController {

    private final AlbumPublicService albumPublicService;
    private final ArtistPublicService artistPublicService;

    @GetMapping("/{artistId}")
    public Mono<ArtistPublicViewDto> getArtist(@PathVariable("artistId") String artistId) {
        return artistPublicService.getById(artistId);
    }

    @GetMapping("/{artistId}/albums")
    public Flux<AlbumPublicStandaloneDto> getAlbumsByArtist(
            @PathVariable("artistId") String artistId,
            @RequestParam(required = false) AlbumType type,
            @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumPublicService.getAllByArtistId(artistId, type, sort);
    }
}
