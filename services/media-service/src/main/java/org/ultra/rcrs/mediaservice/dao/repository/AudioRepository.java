package org.ultra.rcrs.mediaservice.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.model.AudioWithTrack;

import java.util.List;
import java.util.UUID;

@Repository
public interface AudioRepository extends JpaRepository<Audio, UUID> {

    @Query("SELECT a.id, a.guid, a.key, a.codec, a.container, a.durationMs, a.bitrate, a.sampleRate, a.byteSize, t.main " +
            "FROM Audio a JOIN TrackToAudio t ON a.guid = t.guid " +
            "WHERE t.trackId = :trackId")
    List<AudioWithTrack> findAllByTrackId(String trackId);

    List<Audio> findAllByGuid(UUID guid);
}
