package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.OthersOnTrack;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OthersOnTrackRepository extends ReactiveCrudRepository<OthersOnTrack, UUID> {

    Mono<OthersOnTrack> findByTrackId(UUID trackId);

}
