package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, UUID> {

    List<PlaylistTrack> findByPlaylistId(String playlistId, Pageable pageable);
}
