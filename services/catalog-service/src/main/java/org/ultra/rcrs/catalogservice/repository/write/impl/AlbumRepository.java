package org.ultra.rcrs.catalogservice.repository.write.impl;


import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class AlbumRepository extends ReactiveAbstractWriteRepository<Album> {

    public AlbumRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, Album.class);
    }

    public Mono<Long> updateStatus(UUID id, EntityStatus status) {
        return template.update(Album.class)
                .matching(query(where("id").is(id).and("status").not(status)))
                .apply(Update.update("status", status));
    }

    public Mono<Album> findById(@Nonnull UUID id) {
        Assert.notNull(id, "id must not be null");

        return template.selectOne(query(where("id").is(id)), Album.class);
    }

    public Mono<Boolean> albumHasStatus(@Nonnull UUID albumId, @Nonnull EntityStatus status) {
        Assert.notNull(albumId, "albumId must not be null");
        Assert.notNull(status, "status must not be null");

        return template.selectOne(query(where("album_id").is(albumId)), Album.class)
                .map(album -> status.equals(album.getStatus()));
    }

    public Flux<Album> findAllReadyForPublishing() {
        return template.select(query(where("status").is(EntityStatus.READY)
                .and("release_date").isNull()
                .or("release_date").lessThan(Instant.now())), Album.class);
    }

    public Mono<Long> updateStatusAndReleaseDate(UUID id, EntityStatus status, Instant releaseDate) {
        return template.update(Album.class)
                .matching(query(where("id").is(id)))
                .apply(Update.update("status", status).set("release_date", releaseDate));
    }


}
