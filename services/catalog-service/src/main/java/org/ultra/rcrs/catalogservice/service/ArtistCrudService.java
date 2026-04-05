package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistPage;
import org.ultra.rcrs.catalogservice.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.service.operations.ArtistConverter;
import org.ultra.rcrs.catalogservice.utils.CoreUtils;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistCrudService {

    private final ArtistRepository artistRepository;
    private final ArtistConverter artistConverter;

    public Mono<ArtistPage> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                .flatMap(artistConverter::toDto);
    }

    public Mono<ArtistPage> createArtist(ArtistCreateRequest dto) {
        var artist = Artist.builder()
                .id(UUID.randomUUID())
                .name(dto.getName())
                .socialLinks(dto.getSocialLinks())
                .avatarS3Key(CoreUtils.getKey(dto.getAvatarUri())).build();

        return artistRepository.insert(artist)
                .doOnNext(artist1 -> log.info("Artist {} was created successfully. Artist UUID {}", artist1.getName(), artist1.getId()))
                .flatMap(artistConverter::toDto);
    }

}
