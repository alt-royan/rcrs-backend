package org.ultra.rcrs.catalogservice.repository.write.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.Artist;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class ArtistRepository extends ReactiveAbstractWriteRepository<Artist> {

    public ArtistRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, Artist.class);
    }

    public Mono<Boolean> existsById(UUID id) {
        return template.exists(query(where("id").is(id)), Artist.class);
    }

}
