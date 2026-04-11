package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.OtherArtistView;
import org.ultra.rcrs.catalogservice.model.write.OtherArtist;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class OtherArtistViewRepository {

    private final R2dbcEntityTemplate template;

    public Flux<OtherArtistView> findAllByTrackId(@Nonnull UUID trackId) {
        Assert.notNull(trackId, "trackId must not be null");

        return template.select(query(where("track_id").is(trackId)), OtherArtistView.class);
    }

}
