package org.ultra.rcrs.libraryservice.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.jpa.SearchHistoryEntry;
import org.ultra.rcrs.libraryservice.model.jpa.SearchHistoryPK;

import java.time.Instant;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntry, SearchHistoryPK> {

    List<SearchHistoryEntry> findByUserIdOrderBySearchedAtDesc(String userId, Pageable pageable);

    long countByUserId(String userId);

    /**
     * Timestamp of the newest entry that is still within the per-user cap, used to
     * trim everything older after a write.
     */
    @Query("SELECT e.searchedAt FROM SearchHistoryEntry e WHERE e.userId = :userId ORDER BY e.searchedAt DESC")
    List<Instant> findSearchedAtDesc(@Param("userId") String userId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM SearchHistoryEntry e WHERE e.userId = :userId AND e.searchedAt < :cutoff")
    int deleteOlderThan(@Param("userId") String userId, @Param("cutoff") Instant cutoff);

    @Modifying
    @Query("DELETE FROM SearchHistoryEntry e WHERE e.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
