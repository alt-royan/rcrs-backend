package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.mediaservice.dao.model.TrackToAudio;
import org.ultra.rcrs.mediaservice.dao.model.TrackToAudioPK;

import java.util.Optional;

@Repository
public interface TrackToAudioRepository extends JpaRepository<TrackToAudio, TrackToAudioPK> {

    Optional<TrackToAudio> findByTrackIdAndMain(String trackId, Boolean main);
}
