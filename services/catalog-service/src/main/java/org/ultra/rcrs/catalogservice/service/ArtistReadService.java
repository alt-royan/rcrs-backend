package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.repository.ArtistByIdRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistReadService {

    private final ArtistByIdRepository artistByIdRepository;

    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistByIdRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                .map(ArtistDto::new);
    }

    public Mono<List<ArtistSimplifyDto>> collectArtists(Collection<UUID> artistsIds) {
        return Flux.fromIterable(artistsIds)
                .flatMap(artistByIdRepository::findById)
                .map(ArtistSimplifyDto::new)
                .collectList();
    }

}
