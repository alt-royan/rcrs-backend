package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.TrackWithoutAlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class TrackWithoutAlbumViewRepository {

    private final R2dbcEntityTemplate template;

    public Flux<TrackWithoutAlbumView> findAllByAlbumIdAndStatusId(@Nonnull UUID albumId, @Nonnull List<EntityStatus> statuses) {
        Assert.notNull(albumId, "albumId must not be null");
        Assert.notNull(statuses, "statuses must not be null");

        return template.select(query(where("album_id").is(albumId).and("status").in(statuses))
                .sort(Sort.by(Sort.Order.asc("track_number"))), TrackWithoutAlbumView.class);
    }

    public Flux<TrackWithoutAlbumView> findAllByAlbumId(@Nonnull UUID albumId) {
        Assert.notNull(albumId, "albumId must not be null");

        return template.select(query(where("album_id").is(albumId))
                .sort(Sort.by(Sort.Order.asc("track_number"))), TrackWithoutAlbumView.class);
    }
}
