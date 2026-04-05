package org.ultra.rcrs.catalogservice.repository.persist;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.cassandra.core.ReactiveCassandraTemplate;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class AlbumPersistRepositoryImpl implements AlbumPersistRepository<Album> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;

    @Override
    public @NonNull <S extends Album> Mono<S> save(@Nonnull S album) {
        Assert.notNull(album.getTitle(), "Title must not be null");
        Assert.notNull(album.getType(), "Type must not be null");
        Assert.notNull(album.getCoverS3Key(), "Cover S3Key must not be null");
        Assert.notNull(album.getExplicit(), "Explicit must not be null");
        Assert.notEmpty(album.getMainArtists(), "At least 1 main artist must be present on the track");

        album.setKey(new Album.AlbumKey(UUID.randomUUID(), EntityStatus.CREATED));
        album.setTotalDurationMs(0);
        album.setTotalTracks(0);
        album.setAvailable(true);
        album.setYear(album.getReleaseDate() == null ? null : OffsetDateTime.ofInstant(album.getReleaseDate(), ZoneId.systemDefault()).getYear());
        album.setFeaturedArtists(new HashSet<>());

        album.getMainArtists().forEach(uuid -> {
            album.getFeaturedArtists().remove(uuid);
        });

        Set<UUID> artists = Stream.concat(album.getMainArtists().stream(), album.getFeaturedArtists().stream()).collect(Collectors.toSet());

        return Flux.fromIterable(artists)
                .flatMap(artistId -> artistRepository.findById(artistId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                ).doOnComplete(() -> log.info("Album {} validation completed successfully. The album has been assigned UUID {}", album.getTitle(), album.getKey().getId()))
                .then(this.saveAll(album));
    }

    private <S extends Album> Mono<S> saveAll(final S album) {
        List<AlbumByArtist> albumByArtistList = new LinkedList<>();
        album.getMainArtists().forEach(uuid -> albumByArtistList.add(new AlbumByArtist(album, uuid, ArtistRole.MAIN_ARTIST)));
        album.getFeaturedArtists().forEach(uuid -> albumByArtistList.add(new AlbumByArtist(album, uuid, ArtistRole.FEATURED_ARTIST)));

        return cassandraTemplate.batchOps()
                .insert(album)
                .insert(albumByArtistList)
                .execute().doOnSuccess(writeResult -> {
                    if (writeResult != null && writeResult.wasApplied()) {
                        log.info("The batch [Album, AlbumByArtist] completed successfully. Album {} with UUID {}", album.getTitle(), album.getKey().getId());
                    }
                }).thenReturn(album);
    }
}
