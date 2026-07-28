package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.Playlist;
import org.ultra.rcrs.playlistservice.model.PlaylistWithCountProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    List<Playlist> findAllByIdIn(List<UUID> ids);

    @Query("SELECT new PlaylistWithCountProjection(" +
            "p.id, p.ownerId, p.title, p.description, p.tags, p.coverS3Key, p.isPrivate, p.type, " +
            "cast(count(pt) as integer), p.createdAt, p.updatedAt) " +
            "FROM Playlist p LEFT JOIN PlaylistTrack pt ON pt.playlistId = p.id " +
            "WHERE p.id = :id " +
            "GROUP BY p.id, p.ownerId, p.title, p.description, p.tags, p.coverS3Key, p.isPrivate, p.type, " +
            "p.createdAt, p.updatedAt")
    Optional<PlaylistWithCountProjection> findByIdWithTrackCount(UUID id);
}
