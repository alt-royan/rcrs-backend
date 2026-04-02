package org.ultra.rcrs.catalogservice.repository.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.catalogservice.repository.AlbumPersistRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AlbumPersistRepositoryImpl implements AlbumPersistRepository<Album> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;

    @Override
    public @NonNull <S extends Album> Mono<S> save(@Nonnull S album) {
        Assert.isTrue(album.getArtists().stream().anyMatch(ArtistWithRole::isMainArtist), "At least 1 main artist must be present on the album");
        Assert.notNull(album.getTitle(), "Title must not be null");
        Assert.notNull(album.getAlbumType(), "Album type must not be null");
        Assert.notNull(album.getImageKey(), "Image must not be null");
        Assert.notNull(album.getReleaseDate(), "Release date must not be null");

        album.setAlbumId(UUID.randomUUID());
        album.setTotalTracks(0);
        album.setTotalDurationMs(0L);
        album.setAvailable(true);

        return Mono.just(album).zipWith(processByArtist(album))
                .flatMap(tuple -> {
                    Album albumById = tuple.getT1();
                    List<AlbumByArtist> albumsByArtist = tuple.getT2();
                    return cassandraTemplate.batchOps()
                            .insert(albumById)
                            .insert(albumsByArtist)
                            .execute();
                })
                .thenReturn(album);
    }

    private Mono<List<AlbumByArtist>> processByArtist(final Album album) {
        Assert.notNull(album.getAlbumId(), "Id must not be null");

        return Flux.fromIterable(album.getArtists()).flatMap(artistWithRole ->
                artistRepository.findById(artistWithRole.getArtistId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistWithRole.getArtistId() + " was not found")))
                        .thenReturn(AlbumByArtist.builder()
                                .key(AlbumByArtist.AlbumByArtistKey.builder()
                                        .artistId(artistWithRole.getArtistId())
                                        .artistRole(artistWithRole.getArtistRole())
                                        .albumId(album.getAlbumId())
                                        .build())
                                .build())
        ).collectList();
    }
}
