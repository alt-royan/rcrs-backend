package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.dto.AlbumAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.AlbumAdminService;
import org.ultra.rcrs.metadata.service.admin.ArtistAdminService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/artists")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;
    private final AlbumAdminService albumAdminService;

    @GetMapping("/{artistId}")
    public Mono<ArtistAdminViewDto> getArtist(@PathVariable("artistId") String artistId) {
        return artistAdminService.getById(artistId);
    }

    @GetMapping
    public Flux<ArtistAdminStandaloneDto> getArtists(
            @RequestParam(required = false) EntityStatus availabilityStatus,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "50") int limit) {
        return artistAdminService.getAll(availabilityStatus, offset, limit);
    }

    @GetMapping("/count")
    public Mono<Long> countArtists(
            @RequestParam(required = false) EntityStatus availabilityStatus) {
        return artistAdminService.count(availabilityStatus);
    }

    @GetMapping("/{artistId}/albums")
    public Flux<AlbumAdminStandaloneDto> getAlbumsByArtist(
            @PathVariable("artistId") String artistId,
            @RequestParam(required = false) AlbumType type,
            @RequestParam(required = false, defaultValue = "asc") String sort) {
        return albumAdminService.getAllByArtistId(artistId, type, sort);
    }
}
