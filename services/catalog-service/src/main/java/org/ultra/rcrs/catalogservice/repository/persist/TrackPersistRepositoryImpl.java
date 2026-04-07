/*
package org.ultra.rcrs.catalogservice.repository.persist;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class TrackPersistRepositoryImpl implements TrackPersistRepository<Track> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;


    @Override
    public @NonNull <S extends Track> Mono<S> save(@Nonnull S track) {
        Assert.notNull(track.getTitle(), "Title must not be null");
        Assert.notNull(track.getTrackNumber(), "Track number must not be null");
        Assert.notNull(track.getExplicit(), "Explicit must not be null");
        Assert.notNull(track.getAlbumId(), "Album id must not be null");
        Assert.notEmpty(track.getMainArtists(), "At least 1 main artist must be present on the track");

        track.setKey(new Track.TrackKey(UUID.randomUUID(), EntityStatus.CREATED));
        track.setDurationMs(0);
        track.setAvailable(true);

        track.getMainArtists().forEach(uuid -> {
            track.getFeaturedArtists().remove(uuid);
        });

        Set<UUID> artists = Stream.concat(track.getMainArtists().stream(), track.getFeaturedArtists().stream()).collect(Collectors.toSet());

        return albumRepository.findById(track.getAlbumId())
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + track.getAlbumId() + " was not found")))
                .thenMany(Flux.fromIterable(artists)
                        .flatMap(artistId -> artistRepository.findById(artistId)
                                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found"))))
                ).doOnComplete(() -> log.info("Track {} validation completed successfully. The track has been assigned UUID {}", track.getTitle(), track.getKey().getId()))
                .then(this.saveAll(track));

    }

    private <S extends Track> Mono<S> saveAll(final S track) {
        TrackByAlbum trackByAlbum = new TrackByAlbum(track);

        List<ArtistToTrack> artistTrackList = new LinkedList<>();
        track.getMainArtists().forEach(uuid -> artistTrackList.add(new ArtistToTrack(track, uuid, ArtistRole.MAIN_ARTIST)));
        track.getFeaturedArtists().forEach(uuid -> artistTrackList.add(new ArtistToTrack(track, uuid, ArtistRole.FEATURED_ARTIST)));

        return cassandraTemplate.batchOps()
                .insert(track)
                .insert(trackByAlbum)
                .insert(artistTrackList)
                .execute().doOnSuccess(writeResult -> {
                    if (writeResult != null && writeResult.wasApplied()) {
                        log.info("The batch [Track, TrackByAlbum, TrackByArtist] completed successfully. Track {} with UUID {} was added into album {}", track.getTitle(), track.getKey().getId(), track.getAlbumId());
                    }
                }).thenReturn(track);
    }
}

*/
