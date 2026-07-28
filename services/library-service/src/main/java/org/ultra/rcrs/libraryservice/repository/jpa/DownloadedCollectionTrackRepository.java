package org.ultra.rcrs.libraryservice.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedCollectionTrack;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedCollectionTrackPK;

import java.util.List;

@Repository
public interface DownloadedCollectionTrackRepository
        extends JpaRepository<DownloadedCollectionTrack, DownloadedCollectionTrackPK> {

    List<DownloadedCollectionTrack> findByUserIdAndEntityTypeAndEntityId(
            String userId, LibraryEntityType entityType, String entityId);

    @Modifying
    @Query("DELETE FROM DownloadedCollectionTrack m " +
            "WHERE m.userId = :userId AND m.entityType = :entityType AND m.entityId = :entityId")
    int deleteMembership(@Param("userId") String userId,
                         @Param("entityType") LibraryEntityType entityType,
                         @Param("entityId") String entityId);

    @Modifying
    @Query("DELETE FROM DownloadedCollectionTrack m WHERE m.userId = :userId AND m.trackId = :trackId")
    int deleteByTrack(@Param("userId") String userId, @Param("trackId") String trackId);

    @Modifying
    @Query("DELETE FROM DownloadedCollectionTrack m WHERE m.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
