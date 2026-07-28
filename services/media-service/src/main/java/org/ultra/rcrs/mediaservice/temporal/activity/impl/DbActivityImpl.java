package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.config.MediaConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.model.TrackToAudio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.dao.repository.TrackToAudioRepository;
import org.ultra.rcrs.mediaservice.enums.Quality;
import org.ultra.rcrs.mediaservice.temporal.activity.DbActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.time.Instant;
import java.util.UUID;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class DbActivityImpl implements DbActivity {

    private final AudioRepository audioRepository;
    private final TrackToAudioRepository trackToAudioRepository;
    private final AudioUploadRepository audioUploadRepository;
    private final MediaConfigurationProperties properties;

    @Override
    @Transactional
    public UUID saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata, Quality quality, String contentType) {
        Audio audio = Audio.builder()
                .guid(guid)
                .key(key)
                .codec(metadata.codec())
                .container(metadata.container())
                .contentType(contentType)
                .durationMs(metadata.durationMs())
                .bitrate(metadata.bitrate())
                .quality(quality)
                .sampleRate(metadata.sampleRate())
                .byteSize(metadata.byteSize())
                .creationTimestamp(Instant.now())
                .build();

        TrackToAudio trackToAudio = TrackToAudio.builder()
                .trackId(trackId)
                .guid(guid)
                .main(main)
                .build();

        Audio saved = audioRepository.save(audio);
        trackToAudioRepository.save(trackToAudio);
        log.info("Audio record saved: id={}, guid={}, trackId={}, container={}, bitrate={}",
                saved.getId(), guid, trackId, metadata.container(), metadata.bitrate());
        return saved.getId();
    }

    @Override
    public UUID saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata, Quality quality) {
        return saveAudio(trackId, guid, main, key, metadata, quality, properties.getAudio().getContentType());
    }

    @Override
    public AudioUpload getAudioUpload(String uid) {
        return audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("AudioUpload", uid));
    }
}
