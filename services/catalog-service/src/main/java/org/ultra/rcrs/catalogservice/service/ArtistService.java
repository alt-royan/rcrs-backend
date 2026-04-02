package org.ultra.rcrs.catalogservice.service;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;

    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                .map(ArtistDto::new);
    }

    public Mono<ArtistDto> registerNewArtist(ArtistRegisterDto dto) {
        var artist = new Artist(dto);
        artist.setArtistId(UUID.randomUUID());

        return artistRepository.insert(artist)
                .map(ArtistDto::new);
    }

    public Mono<List<ArtistSimplifyDto>> collectArtistsByRole(@Nonnull Collection<ArtistWithRole> artists, @Nonnull ArtistRole role) {
        Objects.requireNonNull(role, "role must not be null here");
        Objects.requireNonNull(artists, "artists must not be null here");

        var ids = artists.stream()
                .filter(artist -> role.equals(artist.getArtistRole()))
                .map(ArtistWithRole::getArtistId)
                .collect(Collectors.toSet());

        return collectArtists(ids);
    }

    public Mono<List<ArtistSimplifyDto>> collectAllArtists(@Nonnull Collection<ArtistWithRole> artists) {
        Objects.requireNonNull(artists, "artists must not be null here");

        var ids = artists.stream().map(ArtistWithRole::getArtistId).collect(Collectors.toSet());

        return collectArtists(ids);
    }

    private Mono<List<ArtistSimplifyDto>> collectArtists(@Nonnull Collection<UUID> artistsIds) {
        Objects.requireNonNull(artistsIds, "artistsIds must not be null here");

        return Flux.fromIterable(artistsIds)
                .flatMap(artistRepository::findById)
                .map(ArtistSimplifyDto::new)
                .collectList();
    }


}
