package org.ultra.rcrs.libraryservice.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedTrack;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedTrackPK;

import java.util.Collection;
import java.util.List;

@Repository
public interface DownloadedTrackRepository extends JpaRepository<DownloadedTrack, DownloadedTrackPK> {

    List<DownloadedTrack> findByUserIdOrderByAddedAtDesc(String userId, Pageable pageable);

    long countByUserId(String userId);

    List<DownloadedTrack> findByUserIdAndTrackIdIn(String userId, Collection<String> trackIds);

    /**
     * Drops the files that a removed collection was the last claim on: anything the
     * user did not download individually and that no remaining collection still
     * lists. Run after the collection's membership rows are gone.
     */
    @Modifying
    @Query("DELETE FROM DownloadedTrack t WHERE t.userId = :userId AND t.explicit = false " +
            "AND NOT EXISTS (SELECT 1 FROM DownloadedCollectionTrack m " +
            "                WHERE m.userId = t.userId AND m.trackId = t.trackId)")
    int deleteOrphaned(@Param("userId") String userId);

    @Modifying
    @Query("DELETE FROM DownloadedTrack t WHERE t.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
