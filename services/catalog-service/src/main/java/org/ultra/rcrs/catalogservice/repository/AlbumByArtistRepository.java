package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.enums.ArtistRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumByArtistRepository extends ReactiveCassandraRepository<AlbumByArtist, AlbumByArtist.AlbumByArtistKey> {

    Flux<AlbumByArtist> findByKeyArtistIdAndArtistRole(UUID artistId, ArtistRole artistRole);

    default Mono<List<AlbumByArtist>> findByArtistId_Main_Asc(UUID artistId) {
        return this.findByKeyArtistIdAndArtistRole(artistId, ArtistRole.MAIN_ARTIST)
                .collectList().map(List::reversed);
    }

    default Mono<List<AlbumByArtist>> findByArtistId_Main(UUID artistId) {
        return this.findByKeyArtistIdAndArtistRole(artistId, ArtistRole.MAIN_ARTIST)
                .collectList();
    }

    default Mono<List<AlbumByArtist>> findByArtistId_AppearsOn(UUID artistId) {
        return this.findByKeyArtistIdAndArtistRole(artistId, ArtistRole.APPEARS_ON)
                .collectList();
    }

}
