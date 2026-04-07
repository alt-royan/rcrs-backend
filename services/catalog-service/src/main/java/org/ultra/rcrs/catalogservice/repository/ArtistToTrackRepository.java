package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;

@Repository
public interface ArtistToTrackRepository extends ReactiveCrudRepository<ArtistToTrack, ArtistToTrack.ArtistToTrackId> {

}
