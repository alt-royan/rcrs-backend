package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistPage;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.service.ArtistCrudService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class ArtistWriteController {

    private final ArtistCrudService artistCrudService;

    @PostMapping
    public Mono<ResponseEntity<ArtistPage>> createNewArtist(@RequestBody @Validated ArtistCreateRequest request) {
        return artistCrudService.createArtist(request)
                .map(ResponseEntity::ok);
    }
}
