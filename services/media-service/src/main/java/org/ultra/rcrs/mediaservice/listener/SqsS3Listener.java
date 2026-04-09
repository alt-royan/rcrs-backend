package org.ultra.rcrs.mediaservice.listener;

import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotification;
import software.amazon.awssdk.eventnotifications.s3.model.S3EventNotificationRecord;
import software.amazon.awssdk.services.sqs.model.Message;

@Component
@Slf4j
@RequiredArgsConstructor
public class SqsS3Listener {

    private final AudioUploadRepository audioUploadRepository;

    @SqsListener("${s3.upload.sqs.queue}")
    @Transactional
    public void handleEventObjectPut(Message message) {
        log.info("Received: {}", message);
        String sqsEventBody = message.body();
        S3EventNotification s3EventNotification = S3EventNotification.fromJson(sqsEventBody);
        S3EventNotificationRecord record = s3EventNotification.getRecords().getFirst();
        if (record.getEventName().contains("Put")) {
            String key = record.getS3().getObject().getKey();
            audioUploadRepository.updateStatusByUid(FileStatus.UPLOADED, key);
            log.info("Object {} was UPLOADED", key);
        }
    }
}
