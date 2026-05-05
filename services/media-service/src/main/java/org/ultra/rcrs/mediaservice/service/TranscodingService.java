package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.kafka.events.StartTrackTranscodingEvent;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.producer.EventProducer;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {

    private final EventProducer eventProducer;
    private final AudioUploadRepository audioUploadRepository;
    private final S3Client s3Client;

    @Value("${s3.upload.bucket}")
    private String s3UploadBucket;

    @Async
    @Transactional
    public void transcode(StartTrackTranscodingEvent event) {
        String uid = event.getUid();
        AudioUpload audio = audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("Audio file", uid));
        audioUploadRepository.updateStatusByUid(FileStatus.TRANSCODING, uid);
        audioUploadRepository.updateExpiredAtByUid(null, uid);
        audioUploadRepository.updateTrackIdAtByUid(event.getTrackId(), uid);
        eventProducer.startTranscoding(event.getTrackId());
        try {

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObject(GetObjectRequest.builder().bucket(s3UploadBucket).key(uid).build(), ResponseTransformer.toBytes());
            byte[] data = objectBytes.asByteArray();
        } catch (Exception e) {
            log.error("Error during transcoding file with uid {}: {}", uid, e.getMessage());
            audioUploadRepository.updateStatusAndErrorByUid(FileStatus.FAILED, e.getMessage(), uid);
            eventProducer.failedTranscoding(event.getTrackId());
            throw e;
        }
    }
}
