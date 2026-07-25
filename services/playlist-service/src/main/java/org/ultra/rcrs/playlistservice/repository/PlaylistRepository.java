package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.Playlist;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, String> {

    List<Playlist> findAllByIdIn(List<String> ids);

    @Override
    @EntityGraph(attributePaths = "tracks")
    Optional<Playlist> findById(String id);
}
