package org.ultra.rcrs.catalogservice.repository.persist;

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
import org.ultra.rcrs.catalogservice.utils.Utils;
import org.ultra.rcrs.enums.TrackStatus;
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
        Assert.notNull(track.getTitle(), "Title must not be null");
        Assert.notNull(track.getReleaseDate(), "Release date must not be null");
        Assert.notNull(track.getTrackNumber(), "Track number must not be null");
        Assert.notNull(track.getExplicit(), "Explicit must not be null");
        Assert.notNull(track.getAlbumId(), "Album id must not be null");
        Assert.notNull(track.getArtists(), "Artists must not be null");
        Assert.notEmpty(track.getArtists().getMainArtists(), "At least 1 main artist must be present on the track");

        track.setKey(new Track.TrackKey(UUID.randomUUID(), TrackStatus.CREATED));
        track.setDurationMs(0);
        track.setAvailable(true);


        return albumRepository.findById(track.getAlbumId())
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + track.getAlbumId() + " was not found")))
                .thenMany(Flux.fromIterable(Utils.flatArtists(track.getArtists()))
                        .doOnNext(artistId -> artistRepository.findById(artistId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found"))))
                ).doOnComplete(() -> log.info("Track validation completed successfully. The track has been assigned UUID {}", track.getKey().getId()))
                .then(this.saveAll(track));

    }

    private <S extends Track> Mono<S> saveAll(final S track) {
        TrackByAlbum trackByAlbum = new TrackByAlbum(track);
        List<TrackByArtist> trackByArtistList = Utils.artistsToMap(track.getArtists()).entrySet().stream()
                .map(entry -> new TrackByArtist(track, entry.getKey(), entry.getValue()))
                .toList();

        return cassandraTemplate.batchOps()
                .insert(track)
                .insert(trackByAlbum)
                .insert(trackByArtistList)
                .execute().doOnSuccess(writeResult -> {
                    if (writeResult != null && writeResult.wasApplied()) {
                        log.info("The batch [Track, TrackByAlbum, TrackByArtist] completed successfully. Track UUID {}", track.getKey().getId());
                    }
                }).thenReturn(track);
    }
}

