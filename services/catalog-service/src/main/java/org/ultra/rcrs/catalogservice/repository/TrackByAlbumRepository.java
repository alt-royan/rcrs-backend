package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackByAlbumRepository extends ReactiveCassandraRepository<TrackByAlbum, TrackByAlbum.TrackByAlbumKey> {

    Mono<List<TrackByAlbum>> findByKeyAlbumId(UUID albumId);
}
