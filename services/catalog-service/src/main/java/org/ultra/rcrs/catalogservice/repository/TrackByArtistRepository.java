package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.TrackByArtist;
import org.ultra.rcrs.enums.ArtistRole;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TrackByArtistRepository extends ReactiveCassandraRepository<TrackByArtist, TrackByArtist.TrackByArtistKey> {

    Flux<TrackByArtist> findByKeyArtistId(UUID artistId);

    Mono<Void> deleteByKeyArtistIdAndArtistRoleAndTrackId(UUID artistId, ArtistRole artistRole, UUID trackId);
}
