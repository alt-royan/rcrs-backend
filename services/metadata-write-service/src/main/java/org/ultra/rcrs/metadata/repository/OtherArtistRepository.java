package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.OtherArtist;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface OtherArtistRepository extends JpaRepository<OtherArtist, UUID> {

    @Modifying
    @Query("DELETE FROM OtherArtist o WHERE o.trackId IN :trackIds")
    void deleteByTrackIdsIn(Collection<UUID> trackIds);
}
