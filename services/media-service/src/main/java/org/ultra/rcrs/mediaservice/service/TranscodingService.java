package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.events.StartTrackTranscodingEvent;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.producer.EventProducer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.OutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscodingService {

    private final EventProducer eventProducer;
    private final AudioUploadRepository audioUploadRepository;
    private final TransactionTemplate transactionTemplate;
    private final S3Client s3Client;

    @Value("${cdn.uploads.bucket}")
    private String s3UploadBucket;

    @Value("${cdn.audios.bucket}")
    private String s3AudioBucket;

    @Value("${cdn.audios.qualities}")
    private int[] audioQualities;

    @Async
    public void transcode(StartTrackTranscodingEvent event) {
        String uid = event.getUid();
        AudioUpload audioUpload = audioUploadRepository.findById(uid)
                .orElseThrow(() -> new NotFoundException("Audio file", uid));
        transactionTemplate.executeWithoutResult(status -> {
            audioUploadRepository.updateStatusByUid(FileStatus.TRANSCODING, uid);
            audioUploadRepository.updateExpiredAtByUid(null, uid);
            audioUploadRepository.updateTrackIdAtByUid(event.getTrackId(), uid);
        });
        eventProducer.startTranscoding(event.getTrackId());
        try {
            ProcessBuilder pb;
            String outputKey;
            for (int quality : audioQualities) {
                pb = createFfmpegProcess(quality);
                outputKey = audioUpload.getUid() + "_" + quality;
                processS3Audio(s3UploadBucket, audioUpload.getUid(), s3AudioBucket, outputKey, pb);
            }
        } catch (Exception e) {
            log.error("Error during transcoding file with uid {}: {}", uid, e.getMessage());
            audioUploadRepository.updateStatusAndErrorByUid(FileStatus.FAILED, e.getMessage(), uid);
            eventProducer.failedTranscoding(event.getTrackId());
            throw new RuntimeException(e);
        }
    }

    private void processS3Audio(String inputBucket, String inputKey, String outputBucket, String outputKey, ProcessBuilder pb) throws IOException, InterruptedException {
        ResponseInputStream<GetObjectResponse> s3Stream = s3Client.getObject(
                GetObjectRequest.builder().bucket(inputBucket).key(inputKey).build());

        Process process = pb.start();
        OutputStream processStdIn = process.getOutputStream();

        s3Stream.transferTo(processStdIn);
        processStdIn.flush();

        process.waitFor();
        var bytes = process.getInputStream().readAllBytes();
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(outputBucket)
                .key(outputKey)
                .contentType("audio/ogg")
                .build(), RequestBody.fromBytes(bytes));
    }

    private ProcessBuilder createFfmpegProcess(int quality) {
        var builder = new ProcessBuilder("ffmpeg", String.format("-i pipe:0 -c:a libvorbis -q:a %d -ar 44100 -vn -map_metadata -1 -y -f ogg pipe:1", quality));
        builder.redirectInput(ProcessBuilder.Redirect.PIPE);
        builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        return builder;
    }
}
