package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.model.DownloadFile;
import org.ultra.rcrs.mediaservice.dao.model.TrackToAudio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.dao.repository.DownloadFileRepository;
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
    private final DownloadFileRepository downloadFileRepository;

    @Override
    @Transactional
    public UUID saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata) {
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

        Audio saved = audioRepository.save(audio);
        trackToAudioRepository.save(trackToAudio);
        log.info("Audio record saved: id={}, guid={}, trackId={}, container={}, bitrate={}",
                saved.getId(), guid, trackId, metadata.container(), metadata.bitrate());
        return saved.getId();
    }

    @Override
    @Transactional
    public void saveDownloadFile(UUID audioId, UUID guid, String key, String fileName, String contentType) {
        DownloadFile downloadFile = DownloadFile.builder()
                .id(audioId)
                .guid(guid)
                .key(key)
                .fileName(fileName)
                .contentType(contentType)
                .creationTimestamp(OffsetDateTime.now())
                .build();

        downloadFileRepository.save(downloadFile);
        log.info("Download file record saved: audioId={}, key={}", audioId, key);
    }

    @Override
    public AudioUpload getAudioUpload(String uid) {
        return audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("AudioUpload", uid));
    }
}
