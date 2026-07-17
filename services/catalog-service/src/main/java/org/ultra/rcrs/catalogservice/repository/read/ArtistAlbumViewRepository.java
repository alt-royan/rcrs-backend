package org.ultra.rcrs.catalogservice.repository.read;


import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.ultra.rcrs.catalogservice.model.read.ArtistAlbumView;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class ArtistAlbumViewRepository {

    private final R2dbcEntityTemplate template;

    public Flux<ArtistAlbumView> findAllByArtist(@Nonnull UUID artistId, @Nonnull List<LifecycleStatus> statuses,
                                                 ArtistRole artistRole, AlbumType type, Sort.Direction direction) {
        Assert.notNull(artistId, "artistId must not be null");
        Assert.notNull(statuses, "statuses must not be null");
        direction = direction == null ? Sort.Direction.DESC : direction;

        var criteria = where("artist_id").is(artistId)
                .and("status").in(statuses);

        if (artistRole != null) {
            criteria = criteria.and("artist_role").is(artistRole);
        }

        if (type != null) {
            criteria = criteria.and("type").is(type);
        }
        return template.select(query(criteria).sort(Sort.by(direction, "release_date")), ArtistAlbumView.class);
    }

    public Flux<ArtistAlbumView> findAllByArtist(@Nonnull UUID artistId, @Nonnull List<LifecycleStatus> statuses, ArtistRole artistRole) {
        return findAllByArtist(artistId, statuses, artistRole, null, Sort.Direction.DESC);
    }

}
