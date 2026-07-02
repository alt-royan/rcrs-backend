package org.ultra.rcrs.mediaservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.time.Instant;
import java.util.List;

@Component
@EnableAsync
@RequiredArgsConstructor
@Slf4j
public class DeleteEntitiesScheduler {

    private final AudioUploadRepository audioUploadRepository;
    private final S3Client s3Client;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Scheduled(fixedRate = 86400000)
    public void scheduleDeleteEntities() {
        List<String> uploads = audioUploadRepository.findAllExpired(Instant.now());
        List<ObjectIdentifier> ois = uploads.stream().map(key -> ObjectIdentifier.builder().key(key).build()).toList();
        DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                .bucket(s3UploadBucket)
                .delete(Delete.builder().objects(ois).build())
                .build();
        s3Client.deleteObjects(deleteObjectsRequest);
        audioUploadRepository.deleteAllByIdInBatch(uploads);
        log.info("Deleted {} expired uploads", uploads.size());
    }
}
