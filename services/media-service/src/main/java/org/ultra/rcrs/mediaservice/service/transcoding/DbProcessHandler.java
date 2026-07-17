package org.ultra.rcrs.mediaservice.service.transcoding;

import lombok.extern.slf4j.Slf4j;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.pipeline.Handler;

@Slf4j
public class DbProcessHandler implements Handler<AudioProcessData, Void> {

    private final AudioRepository audioRepository;

    public DbProcessHandler(AudioRepository audioRepository) {
        this.audioRepository = audioRepository;
    }

    @Override
    public Void process(AudioProcessData input) {
        try {
            Audio audio = Audio.builder()
                    .guid(input.getGuid())
                    .upload_uid(input.getInputKey())
                    .trackId(input.getTrackId())
                    .codec(input.getCodec())
                    .container(input.getContainer())
                    .bitrate(input.getBitrate())
                    .build();
            audioRepository.save(audio);
            log.info("Successfully upload audio with id {}", input.getGuid());
            return null;
        } catch (Exception e) {
            log.error("Exception while uploading audio with id {}", input.getGuid());
            throw new RuntimeException(e);
        }
    }
}
