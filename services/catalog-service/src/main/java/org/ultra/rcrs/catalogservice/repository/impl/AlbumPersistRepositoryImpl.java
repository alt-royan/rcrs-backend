package org.ultra.rcrs.catalogservice.repository.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.ReactiveCassandraBatchOperations;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.model.AlbumByArtist;
import org.ultra.rcrs.catalogservice.model.key.AlbumByArtistKey;
import org.ultra.rcrs.catalogservice.repository.AlbumPersistRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.enums.AlbumGroup;
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
        Assert.notEmpty(album.getArtistIds(), "At least 1 artist must be present on the album");
        Assert.notNull(album.getReleaseDate(), "Release date must not be null");
        Assert.notNull(album.getTitle(), "Title must not be null");
        Assert.notNull(album.getAlbumType(), "Album type must not be null");
        Assert.notNull(album.getImageKey(), "Image must not be null");

        album.setAlbumId(UUID.randomUUID());
        album.setTotalTracks(0);

        return processByArtist(album)
                .mergeWith(Flux.just(album))
                .buffer()
                .flatMap(list -> cassandraTemplate.batchOps().insert(list).execute())
                .then().thenReturn(album);
    }

    private Flux<Object> processByArtist(final Album album) {
        Assert.notNull(album.getAlbumId(), "Id must not be null");
        return Flux.fromIterable(album.getArtistIds()).flatMap(artistId ->
                artistRepository.existsById(artistId).map(exists -> {
                    if (!exists) {
                        throw new NotFoundException("Artist with id " + artistId + " was not found");
                    } else {
                        return AlbumByArtist.builder()
                                .key(AlbumByArtistKey.builder()
                                        .artistId(artistId)
                                        .releaseDate(album.getReleaseDate())
                                        .albumId(album.getAlbumId())
                                        .build())
                                .title(album.getTitle())
                                .albumType(album.getAlbumType())
                                .albumGroup(AlbumGroup.MAINLINE)
                                .artistIds(album.getArtistIds())
                                .imageKey(album.getImageKey())
                                .totalTracks(album.getTotalTracks())
                                .build();
                    }
                }));
    }
}
