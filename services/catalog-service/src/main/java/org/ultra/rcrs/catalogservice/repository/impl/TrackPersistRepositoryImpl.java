package org.ultra.rcrs.catalogservice.repository.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.catalogservice.model.track.TrackByArtist;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackPersistRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TrackPersistRepositoryImpl implements TrackPersistRepository<Track> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;


    @Override
    public @NonNull <S extends Track> Mono<S> save(@Nonnull S track) {
        Assert.notNull(track.getArtists(), "Artists must not be null");
        Assert.notNull(track.getTitle(), "Title must not be null");
        Assert.notNull(track.getAlbumId(), "Album id must not be null");
        Assert.notNull(track.getTrackNumber(), "Track number must not be null");
        Assert.isTrue(track.getArtists().stream().anyMatch(ArtistWithRole::isMainArtist), "At least 1 main artist must be present on the track");

        track.setTrackId(UUID.randomUUID());
        track.setDurationMs(0L);
        track.setAvailable(true);

        return Mono.just(track).zipWith(processByAlbum(track)).zipWith(processByArtist(track))
                .flatMap(tuple -> {
                    Track trackById = tuple.getT1().getT1();
                    TrackByAlbum trackByAlbum = tuple.getT1().getT2();
                    List<TrackByArtist> tracksByArtist = tuple.getT2();
                    return cassandraTemplate.batchOps()
                            .insert(trackById)
                            .insert(trackByAlbum)
                            .insert(tracksByArtist).execute();
                })
                .thenReturn(track);
    }

    private Mono<TrackByAlbum> processByAlbum(final Track track) {
        Assert.notNull(track.getTrackId(), "Id must not be null");

        return albumRepository.findById(track.getAlbumId())
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + track.getAlbumId() + " was not found")))
                .thenReturn(TrackByAlbum.builder()
                        .key(TrackByAlbum.TrackByAlbumKey.builder()
                                .albumId(track.getAlbumId())
                                .trackNumber(track.getTrackNumber())
                                .trackId(track.getTrackId())
                                .build())
                        .build());

    }

    private Mono<List<TrackByArtist>> processByArtist(final Track track) {
        return Flux.fromIterable(track.getArtists())
                .flatMap(artistWithRole -> artistRepository.findById(artistWithRole.getArtistId())
                        .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistWithRole.getArtistId() + " was not found")))
                        .thenReturn(TrackByArtist.builder()
                                .key(TrackByArtist.TrackByArtistKey.builder()
                                        .artistId(artistWithRole.getArtistId())
                                        .artistRole(artistWithRole.getArtistRole())
                                        .trackId(track.getTrackId())
                                        .build())
                                .build())
                ).collectList();
    }


}

