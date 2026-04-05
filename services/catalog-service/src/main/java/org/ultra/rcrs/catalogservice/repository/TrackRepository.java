package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.repository.persist.TrackPersistRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackRepository extends ReactiveCassandraRepository<Track, Track.TrackKey>, TrackPersistRepository<Track> {

    @Query("SELECT * FROM tracks WHERE id = ? and status IN ?")
    Mono<Track> findByIdAndStatusIn(UUID id, List<EntityStatus> statuses);

}
