package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.ArtistToTrack;
import org.ultra.rcrs.metadata.model.ArtistToTrackPK;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ArtistToTrackRepository extends JpaRepository<ArtistToTrack, ArtistToTrackPK> {

    @Modifying
    @Query("DELETE FROM ArtistToTrack at WHERE at.trackId IN :trackIds")
    void deleteByTrackIdsIn(Collection<UUID> trackIds);
}
