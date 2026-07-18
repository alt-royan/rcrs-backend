package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.ArtistToTrackPK;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistToTrackRepository extends JpaRepository<ArtistToTrack, ArtistToTrackPK> {
}
