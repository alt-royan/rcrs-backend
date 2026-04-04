package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ArtistRepository extends ReactiveCassandraRepository<Artist, UUID> {

    @Query("SELECT * FROM artists WHERE id IN ?")
    Flux<Artist> findAllByIdIn(Collection<UUID> ids);

}
