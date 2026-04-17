package org.ultra.rcrs.catalogservice.repository.write.impl;


import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.TrackView;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class TrackRepository extends ReactiveAbstractWriteRepository<Track> {

    public TrackRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, Track.class);
    }

    public Mono<Long> updateStatus(UUID id, EntityStatus status) {
        return template.update(Track.class)
                .matching(query(where("id").is(id)))
                .apply(Update.update("status", status));
    }

    public Mono<Track> findById(@Nonnull UUID id) {
        Assert.notNull(id, "id must not be null");

        return template.selectOne(query(where("id").is(id)), Track.class);
    }
}
