package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.AlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class AlbumViewRepository {

    private final R2dbcEntityTemplate template;

    public Mono<AlbumView> findByIdAndStatusIn(@Nonnull UUID id, @Nonnull List<EntityStatus> statuses) {
        Assert.notNull(id, "id must not be null");
        Assert.notNull(statuses, "statuses must not be null");

        return template.selectOne(query(where("id").is(id).and("status").in(statuses)), AlbumView.class);
    }

}
