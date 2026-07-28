package org.ultra.rcrs.libraryservice.repository.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedCollection;
import org.ultra.rcrs.libraryservice.model.jpa.DownloadedCollectionPK;

import java.util.List;

@Repository
public interface DownloadedCollectionRepository extends JpaRepository<DownloadedCollection, DownloadedCollectionPK> {

    List<DownloadedCollection> findByUserIdOrderByAddedAtDesc(String userId, Pageable pageable);

    long countByUserId(String userId);

    @Modifying
    @Query("DELETE FROM DownloadedCollection c WHERE c.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);
}
