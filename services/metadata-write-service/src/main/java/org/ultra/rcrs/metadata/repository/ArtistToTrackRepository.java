package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.ArtistToTrack;
import org.ultra.rcrs.metadata.model.ArtistToTrackPK;

@Repository
public interface ArtistToTrackRepository extends JpaRepository<ArtistToTrack, ArtistToTrackPK> {
}
