package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.repository.persist.AlbumPersistRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumRepository extends ReactiveCassandraRepository<Album, Album.AlbumKey>, AlbumPersistRepository<Album> {

    @Query("SELECT * FROM albums WHERE id = :id")
    Mono<Album> findById(UUID id);

    @Query("SELECT * FROM albums WHERE id = :id and status IN :statuses")
    Mono<Album> findByIdAndStatusIn(UUID id, List<EntityStatus> statuses);

    @Query("UPDATE albums SET total_tracks = total_tracks + 1 WHERE id = :id")
    Mono<Void> incTotalTracks(UUID id);
}
