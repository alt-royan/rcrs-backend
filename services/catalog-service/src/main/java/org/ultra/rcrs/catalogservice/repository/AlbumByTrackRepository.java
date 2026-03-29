package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.AlbumByTrack;
import org.ultra.rcrs.catalogservice.model.key.AlbumByTrackKey;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AlbumByTrackRepository extends ReactiveCassandraRepository<AlbumByTrack, AlbumByTrackKey> {

    Mono<AlbumByTrack> findByKeyTrackId(UUID trackId);
}
