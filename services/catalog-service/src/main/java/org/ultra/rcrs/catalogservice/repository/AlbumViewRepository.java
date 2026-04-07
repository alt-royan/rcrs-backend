package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.read.AlbumView;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumViewRepository extends ReactiveCrudRepository<AlbumView, UUID> {
    Mono<AlbumView> findByIdAndStatusIn(UUID id, List<EntityStatus> statuses);
}
