package org.ultra.rcrs.catalogservice.repository.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.repository.AlbumPersistRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AlbumPersistRepositoryImpl implements AlbumPersistRepository<Album> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;

    @Override
    public @NonNull <S extends Album> Mono<S> save(@Nonnull S album) {
        Assert.isTrue(album.getArtists().containsValue(ArtistRole.MAIN_ARTIST),
                "At least 1 main artist must be present on the album");
        Assert.notNull(album.getTitle(), "Title must not be null");
        Assert.notNull(album.getAlbumType(), "Album type must not be null");
        Assert.notNull(album.getImageKey(), "Image must not be null");
        Assert.notNull(album.getReleaseDate(), "Release date must not be null");

        album.setAlbumId(UUID.randomUUID());
        album.setTotalTracks(0);
        album.setTotalDurationMs(0L);

        return processByArtist(album)
                .mergeWith(Flux.just(album))
                .collectList()
                .flatMap(list -> cassandraTemplate.batchOps().insert(list, InsertOptions.builder()
                        .ifNotExists(true)).execute())
                .thenReturn(album);
    }

    private Flux<Object> processByArtist(final Album album) {
        Assert.notNull(album.getAlbumId(), "Id must not be null");
        return Flux.fromIterable(album.getArtists().entrySet()).flatMap(entry ->
                artistRepository.findById(entry.getKey())
                        .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + entry.getKey() + " was not found")))
                        .map(artist -> AlbumByArtist.builder()
                                .key(AlbumByArtist.AlbumByArtistKey.builder()
                                        .artistId(entry.getKey())
                                        .artistRole(entry.getValue())
                                        .releaseDate(album.getReleaseDate())
                                        .albumId(album.getAlbumId())
                                        .build())
                                .title(album.getTitle())
                                .albumType(album.getAlbumType())
                                .artists(album.getArtists())
                                .imageKey(album.getImageKey())
                                .totalTracks(album.getTotalTracks())
                                .build()));
    }
}
