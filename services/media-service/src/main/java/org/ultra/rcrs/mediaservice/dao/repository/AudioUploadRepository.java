package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;

import java.time.Instant;
import java.util.List;

@Repository
public interface AudioUploadRepository extends JpaRepository<AudioUpload, String> {

    @Transactional
    @Modifying
    @Query("UPDATE AudioUpload SET status=:status WHERE uid = :uid")
    void updateStatusByUid(FileStatus status, String uid);

    @Transactional
    @Modifying
    @Query("UPDATE AudioUpload SET expiresAt=:expiresAt WHERE uid = :uid")
    void updateExpiredAtByUid(Instant expiresAt, String uid);

    @Transactional
    @Modifying
    @Query("UPDATE AudioUpload SET trackId=:trackId WHERE uid = :uid")
    void updateTrackIdAtByUid(String trackId, String uid);

    @Transactional
    @Modifying
    @Query("UPDATE AudioUpload SET status=:status, error=:error WHERE uid = :uid")
    void updateStatusAndErrorByUid(FileStatus status, String error, String uid);

    @Query("""
            SELECT a.uid
            FROM AudioUpload a
            WHERE a.expiresAt IS NOT NULL
              AND a.expiresAt < :now
            """)
    List<String> findAllExpired(Instant now);

    @Query("""
            SELECT a.uid
            FROM AudioUpload a
            WHERE a.status = :status
              AND a.expiresAt IS NOT NULL
              AND a.expiresAt < :now
            """)
    List<String> findExpiredByStatus(FileStatus status, Instant now);

    @Query("""
            SELECT a.uid
            FROM AudioUpload a
            WHERE a.status = :status
              AND a.createdAt < :threshold
            """)
    List<String> findStaleByStatus(FileStatus status, Instant threshold);
}
