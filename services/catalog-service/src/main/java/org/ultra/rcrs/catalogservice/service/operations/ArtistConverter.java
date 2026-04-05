package org.ultra.rcrs.catalogservice.service.operations;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistPage;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistSimple;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.service.MediaServiceClient;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistConverter {

    private final ArtistRepository artistRepository;
    private final MediaServiceClient mediaServiceClient;

    public Mono<Set<ArtistSimple>> collectArtistsSimple(@Nonnull Iterable<UUID> ids) {
        Objects.requireNonNull(ids, "ids must not be null here");

        return artistRepository.findAllById(ids)
                .flatMap(artist -> mediaServiceClient.fetchImageUrl(artist.getAvatarS3Key())
                        .map(url -> ArtistSimple.builder()
                                .id(Url62.encode(artist.getId()))
                                .name(artist.getName())
                                .avatarUrl(url).build()))
                .collect(Collectors.toSet());
    }

    public Mono<ArtistPage> toDto(Artist artist) {
        return mediaServiceClient.fetchImageUrl(artist.getAvatarS3Key())
                .map(url -> ArtistPage.builder()
                        .id(Url62.encode(artist.getId()))
                        .name(artist.getName())
                        .socialLinks(artist.getSocialLinks())
                        .avatarUrl(url).build());
    }

}
