package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistCrudService {

    private final ArtistRepository artistRepository;
    private final ArtistConverter artistConverter;

    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                .map(artistConverter::toDto);
    }

    @Transactional
    public Mono<ArtistDto> createArtist(ArtistCreateRequest dto) {
        return artistRepository.save(artistConverter.requestToEntity(dto))
                .doOnNext(artist1 -> log.info("Artist {} was created successfully. Artist UUID {}", artist1.getName(), artist1.getId()))
                .map(artistConverter::toDto);
    }

}
