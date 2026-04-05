package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackByAlbumRepository extends ReactiveCassandraRepository<TrackByAlbum, TrackByAlbum.TrackByAlbumKey> {

    @Query("SELECT * FROM tracks_by_album WHERE albumId = ? and track_status IN ? ORDER BY track_number ASC")
    Flux<TrackByAlbum> findAll(UUID albumId, List<EntityStatus> trackStatuses);
}
