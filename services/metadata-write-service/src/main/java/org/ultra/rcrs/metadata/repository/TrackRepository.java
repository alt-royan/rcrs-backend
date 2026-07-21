package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    @Modifying
    @Query("UPDATE Track t SET t.lifecycleStatus = :status WHERE t.id = :id")
    void updateLifecycleStatusById(LifecycleStatus status, UUID id);

    @Modifying
    @Query("UPDATE Track t SET t.lifecycleStatus = :status, t.durationMs = :durationMs WHERE t.id = :id")
    void updateLifecycleStatusAndDurationById(LifecycleStatus status, Integer durationMs, UUID id);

    @Modifying
    @Query("UPDATE Track t SET t.availabilityStatus = :status WHERE t.id = :id")
    void updateAvailabilityStatusById(EntityStatus status, UUID id);

    @Query("SELECT t.id FROM Track t WHERE t.availabilityStatus = :status")
    List<UUID> findAllByAvailabilityStatus(EntityStatus status);

    @Query("SELECT t.id FROM Track t WHERE t.albumId = :albumId")
    List<UUID> findAllIdsByAlbumId(UUID albumId);
}
