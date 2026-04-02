package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.enums.AlbumsOrder;
import org.ultra.rcrs.enums.ArtistRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface AlbumByArtistRepository extends ReactiveCassandraRepository<AlbumByArtist, AlbumByArtist.AlbumByArtistKey> {

    Mono<Void> deleteByKeyArtistIdAndArtistRoleAndAlbumId(UUID artistId, ArtistRole artistRole, UUID albumId);

    Flux<AlbumByArtist> findByKeyArtistIdAndArtistRole(UUID artistId, ArtistRole artistRole);

    default Flux<AlbumByArtist> findByArtistId_Main(UUID artistId, AlbumsOrder order) {
        return this.findByKeyArtistIdAndArtistRole(artistId, ArtistRole.MAIN_ARTIST)
                .collectList().map(list -> order == AlbumsOrder.ASC ? list.reversed() : list)
                .flatMapMany(Flux::fromIterable);
    }

    default Flux<AlbumByArtist> findByArtistId_AppearsOn(UUID artistId, AlbumsOrder order) {
        return this.findByKeyArtistIdAndArtistRole(artistId, ArtistRole.APPEARS_ON)
                .collectList().map(list -> order == AlbumsOrder.ASC ? list.reversed() : list)
                .flatMapMany(Flux::fromIterable);
    }

}
