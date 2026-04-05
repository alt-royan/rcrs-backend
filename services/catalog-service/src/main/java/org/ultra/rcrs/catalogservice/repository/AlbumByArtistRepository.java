package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumByArtistRepository extends ReactiveCassandraRepository<AlbumByArtist, AlbumByArtist.AlbumByArtistKey> {

    @Query("SELECT * FROM albums_by_artist WHERE artistId = ? and album_status IN ? and artist_role IN ? and album_type IN ? ORDER BY release_date ?")
    Flux<AlbumByArtist> findAll(UUID artistId, List<EntityStatus> albumStatuses, ArtistRole[] artistRoles, AlbumType[] albumTypes, Sort.Direction direction);

    @Query("UPDATE albums_by_artist SET totalTracks = totalTracks + 1 WHERE id = ?")
    Mono<Void> incTotalTracks(UUID id);

}
