package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.AlbumByArtist;
import org.ultra.rcrs.catalogservice.model.key.AlbumByArtistKey;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface AlbumByArtistRepository extends ReactiveCassandraRepository<AlbumByArtist, AlbumByArtistKey> {

    Flux<AlbumByArtist> findByKeyArtistId(UUID artistId);

 /*   private final CassandraTemplate cassandraTemplate;

    @Override
    @Nonnull
    public <S extends Artist> Mono<S> insert(final @Nonnull S artist){
        final CassandraBatchOperations batchOps = cassandraTemplate.batchOps();
        batchOps.insert(artist);
        batchOps.execute();
        return movie;
    }*/
}
