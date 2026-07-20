package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    @Modifying
    @Query("UPDATE Track t SET t.lifecycleStatus = :status WHERE t.id = :id")
    void updateLifecycleStatusById(LifecycleStatus status, UUID id);

    @Modifying
    @Query("UPDATE Track t SET t.availabilityStatus = :status WHERE t.id = :id")
    void updateAvailabilityStatusById(EntityStatus status, UUID id);

    @Modifying
    @Query("UPDATE Track t SET t.availabilityStatus = :status WHERE t.albumId = :albumId")
    void updateAvailabilityStatusByAlbumId(EntityStatus status, UUID albumId);

    @Modifying
    @Query("UPDATE Track t SET t.availabilityStatus = :status WHERE t.albumId IN (SELECT ata.albumId FROM ArtistToAlbum ata WHERE ata.artistId = :artistId)")
    void updateAvailabilityStatusByArtistId(EntityStatus status, UUID artistId);
}
