package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.model.Artist;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistRegisterService {

    private final ArtistRepository artistRepository;

    public Mono<ArtistDto> registerNewArtist(ArtistRegisterDto dto) {
        return artistRepository.insert(Artist.builder()
                        .artistId(UUID.randomUUID())
                        .name(dto.getName())
                        .bio(dto.getBio())
                        .imageKey(dto.getImageExternalKey())
                        .build())
                .map(ArtistDto::new);
    }

}
