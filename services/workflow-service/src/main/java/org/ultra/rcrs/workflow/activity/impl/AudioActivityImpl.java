package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.workflow.activity.AudioActivity;
import org.ultra.rcrs.workflow.client.AudioClient;
import org.ultra.rcrs.workflow.dto.AudioStatusResponse;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ActivityImpl
public class AudioActivityImpl implements AudioActivity {

    private static final Set<FileStatus> VALID_STATUSES = Set.of(FileStatus.UPLOADED);

    private final AudioClient audioClient;

    public AudioActivityImpl(AudioClient audioClient) {
        this.audioClient = audioClient;
    }

    @Override
    public void checkAllAudiosUploaded(List<String> uids) {
        var res = audioClient.getAudioStatus(uids);
        if (res == null || !res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new RuntimeException("Failed to fetch audio status from media-service");
        }

        List<AudioStatusResponse> statuses = res.getBody();

        Set<String> foundUids = statuses.stream()
                .map(AudioStatusResponse::uid)
                .collect(Collectors.toSet());

        List<String> missing = uids.stream()
                .filter(uid -> !foundUids.contains(uid))
                .toList();
        if (!missing.isEmpty()) {
            throw new RuntimeException("Audio records not found for uids: " + missing);
        }

        List<String> notReady = statuses.stream()
                .filter(s -> !VALID_STATUSES.contains(s.status()))
                .map(s -> s.uid() + "(" + s.status() + ")")
                .toList();
        if (!notReady.isEmpty()) {
            throw new RuntimeException("Audios are not uploaded yet: " + notReady);
        }
    }
}
