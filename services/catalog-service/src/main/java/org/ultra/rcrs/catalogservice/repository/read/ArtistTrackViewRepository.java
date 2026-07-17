package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.ArtistTrackView;
import org.ultra.rcrs.enums.LifecycleStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ArtistTrackViewRepository {

    private final R2dbcEntityTemplate template;

    public Flux<ArtistTrackView> findAllByArtist(@Nonnull UUID artistId, @Nonnull List<LifecycleStatus> statuses, Sort.Direction direction) {
        Assert.notNull(artistId, "artistId must not be null");
        Assert.notNull(statuses, "statuses must not be null");

        direction = direction == null ? Sort.Direction.DESC : direction;

        var criteria = where("artist_id").is(artistId)
                .and("status").in(statuses);

        return template.select(query(criteria).sort(Sort.by(direction, "release_date")), ArtistTrackView.class);
    }

    public Flux<ArtistTrackView> findAllByArtist(@Nonnull UUID artistId, @Nonnull List<LifecycleStatus> statuses) {
        return findAllByArtist(artistId, statuses, Sort.Direction.DESC);
    }

}
