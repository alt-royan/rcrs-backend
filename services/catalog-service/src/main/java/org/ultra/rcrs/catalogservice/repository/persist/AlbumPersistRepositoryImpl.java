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
import org.ultra.rcrs.catalogservice.utils.Utils;
import org.ultra.rcrs.enums.AlbumStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Year;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AlbumPersistRepositoryImpl implements AlbumPersistRepository<Album> {

    private final ReactiveCassandraTemplate cassandraTemplate;
    private final ArtistRepository artistRepository;

    @Override
    public @NonNull <S extends Album> Mono<S> save(@Nonnull S album) {
        Assert.notNull(album.getTitle(), "Title must not be null");
        Assert.notNull(album.getType(), "Type must not be null");
        Assert.notNull(album.getReleaseDate(), "Release date must not be null");
        Assert.notNull(album.getCoverS3Key(), "Cover S3Key must not be null");
        Assert.notNull(album.getExplicit(), "Explicit must not be null");
        Assert.notNull(album.getArtists(), "Artists must not be null");
        Assert.notEmpty(album.getArtists().getMainArtists(), "At least 1 main artist must be present on the track");

        album.setKey(new Album.AlbumKey(UUID.randomUUID(), AlbumStatus.CREATED));
        album.setTotalDurationMs(0);
        album.setTotalTracks(0);
        album.setAvailable(true);
        album.setYear(Year.of(album.getReleaseDate().getYear()));

        return Flux.fromIterable(Utils.flatArtists(album.getArtists()))
                .doOnNext(artistId -> artistRepository.findById(artistId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                ).doOnComplete(() -> log.info("Album validation completed successfully. The album has been assigned UUID {}", album.getKey().getId()))
                .then(this.saveAll(album));
    }

    private <S extends Album> Mono<S> saveAll(final S album) {
        List<AlbumByArtist> albumByArtistList = Utils.artistsToMap(album.getArtists()).entrySet().stream()
                .map(entry -> new AlbumByArtist(album, entry.getKey(), entry.getValue()))
                .toList();

        return cassandraTemplate.batchOps()
                .insert(album)
                .insert(albumByArtistList)
                .execute().doOnSuccess(writeResult -> {
                    if (writeResult != null && writeResult.wasApplied()) {
                        log.info("The batch [Album, AlbumByArtist] completed successfully. Album UUID {}", album.getKey().getId());
                    }
                }).thenReturn(album);
    }
}
