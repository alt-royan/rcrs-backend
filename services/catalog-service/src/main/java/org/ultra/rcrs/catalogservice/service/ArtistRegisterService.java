package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.repository.ArtistByIdRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ArtistRegisterService {

    private final ArtistByIdRepository artistByIdRepository;

    public Mono<ArtistDto> registerNewArtist(ArtistRegisterDto artistRegisterDto){
        return null;
    }

}
