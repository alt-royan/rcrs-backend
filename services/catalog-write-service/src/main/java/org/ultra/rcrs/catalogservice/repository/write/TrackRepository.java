package org.ultra.rcrs.catalogservice.repository.write;

import jakarta.annotation.Nonnull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrackRepository extends JpaRepository<Track, UUID> {

    @Modifying
    @Query("UPDATE Track t SET t.status = :status WHERE t.id IN :ids AND t.status <> :status")
    int updateStatusByIds(@Param("ids") List<UUID> ids, @Param("status") EntityStatus status);

    @Modifying
    @Query("UPDATE Track t SET t.status = :status WHERE t.albumId = :albumId")
    int updateStatusForAllInAlbum(@Param("albumId") UUID albumId, @Param("status") EntityStatus status);

    @Modifying
    @Query("UPDATE Track t SET t.status = :status, t.releaseDate = :releaseDate WHERE t.id = :id")
    int updateStatusAndReleaseDate(@Param("id") UUID id, @Param("status") EntityStatus status, @Param("releaseDate") Instant releaseDate);

    List<Track> findAllByAlbumId(UUID albumId);

    @Query("SELECT COUNT(t) FROM Track t WHERE t.albumId = :albumId AND t.status = :status")
    long countByAlbumIdAndStatus(@Param("albumId") UUID albumId, @Param("status") EntityStatus status);

    @Query("SELECT COUNT(t) FROM Track t WHERE t.albumId = :albumId")
    long countByAlbumId(@Param("albumId") UUID albumId);

    @Query("SELECT t FROM Track t WHERE t.status = :status AND (t.releaseDate IS NULL OR t.releaseDate < :now)")
    List<Track> findAllReadyForPublishing(@Param("status") EntityStatus status, @Param("now") Instant now);
}
