package org.ultra.rcrs.catalogservice.repository.impl;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.InsertOptions;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackPersistRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class TrackPersistRepositoryImpl implements TrackPersistRepository<Track> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;

    @Override
    public @NonNull <S extends Track> Mono<S> save(@Nonnull S track) {
        Assert.notNull(track.getTitle(), "Title must not be null");
        Assert.notNull(track.getAlbumId(), "Album id must not be null");
        Assert.notNull(track.getTrackNumber(), "Track number must not be null");
        Assert.isTrue(track.getArtists().containsValue(ArtistRole.MAIN_ARTIST),
                "At least 1 main artist must be present on the track");

        track.setTrackId(UUID.randomUUID());
        track.setDurationMs(0L);

        return processByAlbum(track)
                .mergeWith(Mono.just(track))
                .collectList()
                .flatMap(list -> cassandraTemplate.batchOps().insert(list, InsertOptions.builder()
                        .ifNotExists(true)).execute())
                .thenReturn(track);
    }

    private Mono<Object> processByAlbum(final Track track) {
        Assert.notNull(track.getTrackId(), "Id must not be null");

        return albumRepository.findById(track.getAlbumId())
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + track.getAlbumId() + " was not found")))
                .thenMany(Flux.fromIterable(track.getArtists().keySet())
                        .flatMap(artistId -> artistRepository.findById(artistId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                        ))
                .then()
                .thenReturn(TrackByAlbum.builder()
                        .key(TrackByAlbum.TrackByAlbumKey.builder()
                                .albumId(track.getAlbumId())
                                .trackNumber(track.getTrackNumber())
                                .build())
                        .title(track.getTitle())
                        .trackId(track.getTrackId())
                        .durationMs(track.getDurationMs())
                        .artists(track.getArtists())
                        .build());

    }
}

