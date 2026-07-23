package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.model.TrackToAudio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.dao.repository.TrackToAudioRepository;
import org.ultra.rcrs.mediaservice.temporal.activity.DbActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class DbActivityImpl implements DbActivity {

    private final AudioRepository audioRepository;
    private final TrackToAudioRepository trackToAudioRepository;
    private final AudioUploadRepository audioUploadRepository;

    @Override
    @Transactional
    public void saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata) {
        Audio audio = Audio.builder()
                .guid(guid)
                .key(key)
                .codec(metadata.codec())
                .container(metadata.container())
                .durationMs(metadata.durationMs())
                .bitrate(metadata.bitrate())
                .sampleRate(metadata.sampleRate())
                .byteSize(metadata.byteSize())
                .creationTimestamp(OffsetDateTime.now())
                .build();

        TrackToAudio trackToAudio = TrackToAudio.builder()
                .trackId(trackId)
                .guid(guid)
                .main(main)
                .build();

        audioRepository.save(audio);
        trackToAudioRepository.save(trackToAudio);
        log.info("Audio record saved: guid={}, trackId={}, container={}, bitrate={}",
                guid, trackId, metadata.container(), metadata.bitrate());
    }

    @Override
    public AudioUpload getAudioUpload(String uid) {
        return audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("AudioUpload", uid));
    }
}
