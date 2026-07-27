package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.model.PlaylistTrackPK;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, PlaylistTrackPK> {

    List<PlaylistTrack> findByPlaylistId(UUID playlistId, Pageable pageable);

    Integer countByPlaylistId(UUID playlistId);

    List<PlaylistTrack> findAllByPlaylistId(UUID playlistId);
}
