package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.Track;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    @Modifying
    @Query("UPDATE Track t SET t.lyfecycle_status = ? WHERE t.id = ?")
    void updateLifecycleStatusById(LifecycleStatus status, UUID id);

    @Modifying
    @Query("UPDATE Track t SET t.availability_status = ? WHERE t.id = ?")
    void updateAvailabilityStatusById(EntityStatus status, UUID id);
}
