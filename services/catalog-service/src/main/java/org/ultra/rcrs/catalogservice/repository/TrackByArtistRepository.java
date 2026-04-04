package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.track.TrackByArtist;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.TrackStatus;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackByArtistRepository extends ReactiveCassandraRepository<TrackByArtist, TrackByArtist.TrackByArtistKey> {

    @Query("SELECT * FROM tracks_by_artist WHERE artistId = ? and track_status IN ? and artist_role IN ? ORDER BY release_date ?")
    Flux<TrackByArtist> findAll(UUID artistId, List<TrackStatus> trackStatuses, List<ArtistRole> artistRoles, Sort.Direction direction);

    default Flux<TrackByArtist> findAllTracks(UUID artistId, List<TrackStatus> trackStatuses, List<ArtistRole> artistRoles, Sort.Direction direction) {
        return findAll(artistId, trackStatuses, List.of(ArtistRole.values()), direction);
    }
}
