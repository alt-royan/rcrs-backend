package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.temporal.activity.SaveAudioRecordActivity;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@ActivityImpl
@Slf4j
public class SaveAudioRecordActivityImpl implements SaveAudioRecordActivity {

    private final AudioRepository audioRepository;

    public SaveAudioRecordActivityImpl(AudioRepository audioRepository) {
        this.audioRepository = audioRepository;
    }

    @Override
    public void save(String guid, String uploadUid, String trackId, AudioMetadata metadata) {
        Audio audio = Audio.builder()
                .guid(UUID.fromString(guid))
                .trackId(trackId)
                .codec(metadata.codec())
                .container(metadata.container())
                .durationMs(metadata.durationMs())
                .bitrate(metadata.bitrate())
                .sampleRate(parseIntOrNull(metadata.sampleRate()))
                .byteSize(metadata.byteSize())
                .creationTimestamp(OffsetDateTime.now())
                .build();

        audioRepository.save(audio);
        log.info("Audio record saved: guid={}, trackId={}, container={}, bitrate={}",
                guid, trackId, metadata.container(), metadata.bitrate());
    }

    private Integer parseIntOrNull(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
