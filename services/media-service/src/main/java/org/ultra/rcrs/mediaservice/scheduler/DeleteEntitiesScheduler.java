package org.ultra.rcrs.mediaservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteEntitiesScheduler {

    private static final Duration STALE_THRESHOLD = Duration.ofHours(24);

    private final AudioUploadRepository audioUploadRepository;
    private final S3Client s3Client;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Scheduled(fixedRate = 3600000)
    public void cleanupStaleUploads() {
        Instant now = Instant.now();
        Instant threshold = now.minus(STALE_THRESHOLD);

        List<String> staleWaiting = audioUploadRepository.findStaleByStatus(FileStatus.WAIT_FOR_UPLOAD, threshold);
        if (!staleWaiting.isEmpty()) {
            audioUploadRepository.deleteAllByIdInBatch(staleWaiting);
            log.info("Deleted {} stale WAIT_FOR_UPLOAD records (no S3 file)", staleWaiting.size());
        }

        List<String> staleUploaded = audioUploadRepository.findExpiredByStatus(FileStatus.UPLOADED, now);
        if (!staleUploaded.isEmpty()) {
            deleteFromS3(staleUploaded);
            audioUploadRepository.deleteAllByIdInBatch(staleUploaded);
            log.info("Deleted {} stale UPLOADED records (DB + S3)", staleUploaded.size());
        }
    }

    private void deleteFromS3(List<String> keys) {
        List<ObjectIdentifier> objectIds = keys.stream()
                .map(key -> ObjectIdentifier.builder().key(key).build())
                .toList();
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(s3UploadBucket)
                .delete(Delete.builder().objects(objectIds).build())
                .build();
        s3Client.deleteObjects(request);
    }
}
