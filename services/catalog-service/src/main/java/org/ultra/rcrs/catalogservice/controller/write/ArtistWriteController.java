package org.ultra.rcrs.catalogservice.controller.write;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterRequest;
import org.ultra.rcrs.catalogservice.service.ArtistService;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
@ConditionalOnProperty(name = "app.write.enabled", havingValue = "true")
public class ArtistWriteController {

    private final ArtistService artistService;

    @PostMapping
    public Mono<ResponseEntity<ArtistDto>> registerNewArtist(@RequestBody @Validated ArtistRegisterRequest artistRegisterRequest) {
        return artistService.registerNewArtist(artistRegisterRequest)
                .map(ResponseEntity::ok);
    }
}
