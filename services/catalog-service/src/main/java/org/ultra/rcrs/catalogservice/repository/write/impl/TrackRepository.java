package org.ultra.rcrs.catalogservice.repository.write.impl;


import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class TrackRepository extends ReactiveAbstractWriteRepository<Track> {

    public TrackRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, Track.class);
    }

    public Mono<Long> updateStatus(UUID id, EntityStatus status) {
        return updateStatus(List.of(id), status);
    }

    public Mono<Long> updateStatus(List<UUID> ids, EntityStatus status) {
        return template.update(Track.class)
                .matching(query(where("id").in(ids).and("status").not(status)))
                .apply(Update.update("status", status));
    }

    public Mono<Long> updateStatusForAllInAlbum(UUID albumId, EntityStatus status) {
        return template.update(Track.class)
                .matching(query(where("album_id").is(albumId)))
                .apply(Update.update("status", status));
    }

    public Mono<Long> updateStatusAndReleaseDate(UUID id, EntityStatus status, Instant releaseDate) {
        return template.update(Track.class)
                .matching(query(where("id").is(id)))
                .apply(Update.update("status", status).set("release_date", releaseDate));
    }

    public Mono<Track> findById(@Nonnull UUID id) {
        Assert.notNull(id, "id must not be null");

        return template.selectOne(query(where("id").is(id)), Track.class);
    }

    public Flux<Track> findAllById(@Nonnull List<UUID> ids) {
        Assert.notNull(ids, "ids must not be null");

        return template.select(query(where("id").in(ids)), Track.class);
    }

    public Flux<Track> findAllByAlbumId(@Nonnull UUID albumId) {
        Assert.notNull(albumId, "albumId must not be null");

        return template.select(query(where("album_id").is(albumId)), Track.class);
    }

    public Mono<Boolean> allTracksInAlbumHaveStatus(@Nonnull UUID albumId, @Nonnull EntityStatus status) {
        Assert.notNull(albumId, "albumId must not be null");
        Assert.notNull(status, "status must not be null");

        return template.count(query(where("album_id").is(albumId)), Track.class)
                .zipWith(template.count(query(where("album_id").is(albumId).and("status").is(status)), Track.class))
                .map(tuple -> tuple.getT2().equals(tuple.getT1()));
    }

    public Flux<Track> findAllReadyForPublishing() {
        return template.select(query(where("status").is(EntityStatus.READY)
                .and("release_date").isNull()
                .or("release_date").lessThan(Instant.now())), Track.class);
    }
}
