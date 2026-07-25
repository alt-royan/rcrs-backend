package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.mediaservice.dao.model.DownloadFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DownloadFileRepository extends JpaRepository<DownloadFile, UUID> {

    Optional<DownloadFile> findByGuid(UUID guid);

    @Query("SELECT d FROM DownloadFile d " +
            "JOIN TrackToAudio t ON t.guid = d.guid " +
            "WHERE t.trackId = :trackId AND t.main = true")
    Optional<DownloadFile> findByTrackId(String trackId);
}
