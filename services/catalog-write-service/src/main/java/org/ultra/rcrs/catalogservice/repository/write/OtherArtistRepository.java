package org.ultra.rcrs.catalogservice.repository.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.OtherArtist;

import java.util.List;
import java.util.UUID;

@Repository
public interface OtherArtistRepository extends JpaRepository<OtherArtist, UUID> {

    long deleteByTrackId(UUID trackId);

    List<OtherArtist> findByTrackId(UUID trackId);
}
