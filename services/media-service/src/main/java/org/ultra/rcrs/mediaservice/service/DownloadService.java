package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.config.DownloadConfigurationProperties;
import org.ultra.rcrs.mediaservice.config.MediaConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.model.DownloadFile;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.DownloadFileRepository;
import org.ultra.rcrs.mediaservice.dto.DownloadFileResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DownloadService {

    private final DownloadFileRepository downloadFileRepository;
    private final AudioRepository audioRepository;
    private final S3Presigner s3Presigner;
    private final MediaConfigurationProperties properties;

    public DownloadFileResponse getByAudioId(UUID audioId) {
        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new NotFoundException("Audio", audioId));
        DownloadFile downloadFile = downloadFileRepository.findByGuid(audio.getGuid())
                .orElseThrow(() -> new NotFoundException("Download file for audio " + audioId + " not found"));
        return toResponse(downloadFile);
    }

    public DownloadFileResponse getByTrackId(String trackId) {
        DownloadFile downloadFile = downloadFileRepository.findByTrackId(trackId)
                .orElseThrow(() -> new NotFoundException("Download file for track " + trackId + " not found"));
        return toResponse(downloadFile);
    }

    private DownloadFileResponse toResponse(DownloadFile downloadFile) {
        DownloadConfigurationProperties downloadProperties = properties.getDownload();

        String contentDisposition = ContentDisposition.attachment()
                .filename(downloadFile.getFileName(), StandardCharsets.UTF_8)
                .build().toString();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(downloadProperties.getBucket().getName())
                .key(downloadFile.getKey())
                .responseContentDisposition(contentDisposition)
                .responseContentType(downloadFile.getContentType())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(downloadProperties.getSignatureDuration())
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toExternalForm();
        Instant expiresAt = presignedRequest.expiration();

        log.info("Presigned download URL for key [{}] expires at [{}]", downloadFile.getKey(), expiresAt);

        return DownloadFileResponse.builder()
                .id(downloadFile.getId())
                .guid(downloadFile.getGuid())
                .fileName(downloadFile.getFileName())
                .contentType(downloadFile.getContentType())
                .url(url)
                .expiresAt(expiresAt)
                .build();
    }
}
