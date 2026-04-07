package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.read.TrackView;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackViewRepository extends ReactiveCrudRepository<TrackView, UUID> {

    Mono<TrackView> findByIdAndStatusIn(UUID id, List<EntityStatus> statuses);
}
