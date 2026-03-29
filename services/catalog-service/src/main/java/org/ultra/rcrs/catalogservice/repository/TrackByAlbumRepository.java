package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.TrackByAlbum;
import org.ultra.rcrs.catalogservice.model.key.TrackByAlbumKey;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TrackByAlbumRepository extends ReactiveCassandraRepository<TrackByAlbum, TrackByAlbumKey> {

    Flux<TrackByAlbum> findByKeyAlbumId(UUID albumId);
}
