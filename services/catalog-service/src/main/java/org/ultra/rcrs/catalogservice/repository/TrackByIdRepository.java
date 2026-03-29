package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.AlbumById;
import org.ultra.rcrs.catalogservice.model.TrackById;

import java.util.UUID;

@Repository
public interface TrackByIdRepository extends ReactiveCassandraRepository<TrackById, UUID> {

    
}
