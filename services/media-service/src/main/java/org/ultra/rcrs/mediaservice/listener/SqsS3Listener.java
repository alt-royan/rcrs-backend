package org.ultra.rcrs.mediaservice.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.services.sqs.model.Message;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class SqsS3Listener {

    private final AudioUploadRepository audioUploadRepository;

    @Value("${s3.upload.file-duration}")
    private Duration duration;

    @SqsListener("${s3.upload.sqs.queue}")
    @Transactional
    public void handleEventObjectPut(Message message) {
        log.info("Received: {}", message);
        String sqsEventBody = message.body();
        S3EventNotification s3EventNotification = S3EventNotification.fromJson(sqsEventBody);
        var records = s3EventNotification.getRecords();
        if (records != null) {
            records.forEach(record -> {
                if (record.getEventName().equals("ObjectCreated:Put")) {
                    String key = record.getS3().getObject().getKey();
                    audioUploadRepository.updateStatusByUid(FileStatus.UPLOADED, key);
                    audioUploadRepository.updateExpiredAtByUid(Instant.now().plusSeconds(duration.toSeconds()), key);
                    log.info("Object {} was UPLOADED", key);
                }
            });
        }
    }
}
