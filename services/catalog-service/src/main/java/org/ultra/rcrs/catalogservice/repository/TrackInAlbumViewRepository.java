package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.read.TrackInAlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackInAlbumViewRepository extends ReactiveCrudRepository<TrackInAlbumView, UUID> {

    @Query("SELECT * FROM track_in_album_view WHERE album_id=:albumId AND status IN :statuses ORDER BY track_number")
    Flux<TrackInAlbumView> findAllByAlbumId(UUID albumId, List<EntityStatus> statuses);
}
