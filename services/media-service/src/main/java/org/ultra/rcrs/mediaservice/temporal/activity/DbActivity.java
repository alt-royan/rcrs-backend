package org.ultra.rcrs.mediaservice.temporal.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.enums.Quality;
import org.ultra.rcrs.mediaservice.temporal.activity.model.AudioMetadata;

import java.util.UUID;

@ActivityInterface
public interface DbActivity {

    @ActivityMethod
    UUID saveAudioWithContentType(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata, Quality quality, String contentType);

    @ActivityMethod
    UUID saveAudio(String trackId, UUID guid, Boolean main, String key, AudioMetadata metadata, Quality quality);

    @ActivityMethod
    AudioUpload getAudioUpload(String uid);
}
