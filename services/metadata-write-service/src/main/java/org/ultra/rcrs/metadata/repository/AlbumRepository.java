package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {

    @Modifying
    @Query("UPDATE Album a SET a.lifecycleStatus = :status WHERE a.id = :id")
    void updateLifecycleStatusById(LifecycleStatus status, UUID id);

    @Modifying
    @Query("UPDATE Album a SET a.availabilityStatus = :status WHERE a.id = :id")
    void updateAvailabilityStatusById(EntityStatus status, UUID id);
}
