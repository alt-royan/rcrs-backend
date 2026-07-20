package org.ultra.rcrs.metadata.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.service.admin.ArtistAdminService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/artists")
@CrossOrigin("*")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;

    @GetMapping("/{artistId}")
    public Mono<ArtistAdminViewDto> getArtist(@PathVariable("artistId") String artistId) {
        return artistAdminService.getById(artistId);
    }
}
