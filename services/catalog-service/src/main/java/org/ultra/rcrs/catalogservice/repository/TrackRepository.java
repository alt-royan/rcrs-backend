package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.Track;

import java.util.UUID;

@Repository
public interface TrackRepository extends ReactiveCassandraRepository<Track, UUID>, TrackPersistRepository<Track> {

}
