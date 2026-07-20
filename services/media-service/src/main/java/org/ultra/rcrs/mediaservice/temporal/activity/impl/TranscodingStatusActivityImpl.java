package org.ultra.rcrs.mediaservice.temporal.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.producer.MediaEventProducer;
import org.ultra.rcrs.mediaservice.temporal.activity.TranscodingStatusActivity;

@Component
@ActivityImpl
@Slf4j
@RequiredArgsConstructor
public class TranscodingStatusActivityImpl implements TranscodingStatusActivity {

    private final AudioUploadRepository audioUploadRepository;
    private final TransactionTemplate transactionTemplate;
    private final MediaEventProducer mediaEventProducer;

    @Override
    public void updateStatusToTranscoding(String uid, String trackId) {
        AudioUpload audioUpload = audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("Audio file", uid));
        transactionTemplate.executeWithoutResult(status -> {
            audioUploadRepository.updateStatusByUid(FileStatus.TRANSCODING, uid);
            audioUploadRepository.updateExpiredAtByUid(null, uid);
            audioUploadRepository.updateTrackIdAtByUid(trackId, uid);
        });
    }

    @Override
    public void updateStatusToFailed(String uid, String error) {
        audioUploadRepository.updateStatusAndErrorByUid(FileStatus.FAILED, error, uid);
    }

    public void updateStatusToComplete(String uid) {
        audioUploadRepository.updateStatusByUid(FileStatus.COMPLETE, uid);
    }

    @Override
    public void notifyTranscodingStarted(String trackId) {
        mediaEventProducer.startTranscoding(trackId);
    }

    @Override
    public void notifyTranscodingFailed(String trackId) {
        mediaEventProducer.failedTranscoding(trackId);
    }

    @Override
    public void notifyTranscodingSuccess(String trackId) {
        mediaEventProducer.successTranscoding(trackId);
    }
}
