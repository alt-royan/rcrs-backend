package org.ultra.rcrs.catalogservice.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.catalogservice.dto.ArtistAdminViewDto;
import org.ultra.rcrs.catalogservice.service.admin.ArtistAdminService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/artists")
public class ArtistAdminController {

    private final ArtistAdminService artistAdminService;

    @GetMapping("/{artistId}")
    public Mono<ResponseEntity<ArtistAdminViewDto>> getArtist(@PathVariable("artistId") String artistId) {
        return artistAdminService.getById(artistId)
                .map(ResponseEntity::ok);
    }
}
