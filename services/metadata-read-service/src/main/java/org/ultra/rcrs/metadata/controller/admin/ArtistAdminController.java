package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.metadata.dto.AlbumAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.ArtistAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/artists")
@CrossOrigin("*")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;
    private final AlbumAdminService albumAdminService;

    @GetMapping("/{artistId}")
    public Mono<ArtistAdminViewDto> getArtist(@PathVariable("artistId") String artistId) {
        return artistAdminService.getById(artistId);
    }

    @GetMapping("/{artistId}/albums")
    public Flux<AlbumAdminStandaloneDto> getAlbumsByArtist(
            @PathVariable("artistId") String artistId,
            @RequestParam(required = false) AlbumType type,
            @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumAdminService.getAllByArtistId(artistId, type, sort);
    }
}
