package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.ArtistById;

import java.util.UUID;

@Repository
public interface ArtistByIdRepository extends ReactiveCassandraRepository<ArtistById, UUID> {


}
