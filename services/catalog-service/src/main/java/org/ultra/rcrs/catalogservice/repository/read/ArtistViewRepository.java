package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.ArtistView;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ArtistViewRepository {

    private final R2dbcEntityTemplate template;

    public Mono<ArtistView> findById(@Nonnull UUID id) {
        Assert.notNull(id, "id must not be null");

        return template.selectOne(query(where("id").is(id)), ArtistView.class);
    }

    public Mono<Boolean> existsById(@Nonnull UUID id) {
        Assert.notNull(id, "id must not be null");

        return template.exists(query(where("id").is(id)), ArtistView.class);
    }
}
