package org.ultra.rcrs.catalogservice.repository.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrackId;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistToTrackRepository extends JpaRepository<ArtistToTrack, ArtistToTrackId> {

    long deleteByTrackId(UUID trackId);

    List<ArtistToTrack> findByTrackId(UUID trackId);
}
