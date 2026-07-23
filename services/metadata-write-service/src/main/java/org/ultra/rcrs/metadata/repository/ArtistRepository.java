package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.model.Artist;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, UUID> {

    @Modifying
    @Query("UPDATE Artist a SET a.availabilityStatus = :status WHERE a.id = :id")
    void updateAvailabilityStatusById(EntityStatus status, UUID id);

    @Query("SELECT a.id FROM Artist a WHERE a.availabilityStatus = :status")
    List<UUID> findAllByAvailabilityStatus(EntityStatus status);
}
