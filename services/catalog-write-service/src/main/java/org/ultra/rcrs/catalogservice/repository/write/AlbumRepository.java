package org.ultra.rcrs.catalogservice.repository.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumRepository extends JpaRepository<Album, UUID> {

    @Modifying
    @Query("UPDATE Album a SET a.status = :status WHERE a.id = :id AND a.status <> :status")
    int updateStatus(@Param("id") UUID id, @Param("status") LifecycleStatus status);

    @Modifying
    @Query("UPDATE Album a SET a.status = :status, a.releaseDate = :releaseDate WHERE a.id = :id")
    int updateStatusAndReleaseDate(@Param("id") UUID id, @Param("status") LifecycleStatus status, @Param("releaseDate") Instant releaseDate);

    @Query("SELECT a FROM Album a WHERE a.status = :status AND (a.releaseDate IS NULL OR a.releaseDate < :now)")
    List<Album> findAllReadyForPublishing(@Param("status") LifecycleStatus status, @Param("now") Instant now);
}
