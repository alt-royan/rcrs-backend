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

    @Query("SELECT * FROM albums WHERE id = ?")
    Mono<Album> findById(UUID id);

    @Query("SELECT * FROM albums WHERE id = ? and status IN ?")
    Mono<Album> findByIdAndStatusIn(UUID id, List<EntityStatus> statuses);

    @Query("UPDATE albums SET totalTracks = totalTracks + 1 WHERE id = ?")
    Mono<Void> incTotalTracks(UUID id);
}
