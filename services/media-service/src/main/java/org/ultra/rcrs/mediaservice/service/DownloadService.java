package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.config.MediaConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.TrackToAudioRepository;
import org.ultra.rcrs.mediaservice.dto.PresignedUrlResponse;
import org.ultra.rcrs.mediaservice.enums.Quality;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DownloadService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final AudioRepository audioRepository;
    private final TrackToAudioRepository trackToAudioRepository;
    private final S3Presigner s3Presigner;
    private final MediaConfigurationProperties properties;

    public PresignedUrlResponse getByAudioId(UUID audioId) {
        Audio audio = audioRepository.findById(audioId)
                .orElseThrow(() -> new NotFoundException("Audio", audioId));
        return presign(audio);
    }

    public PresignedUrlResponse getByTrackId(String trackId, Quality quality) {
        String bitrate = switch (quality) {
            case LOW -> "128k";
            case MID -> "192k";
            case HIGH -> "320k";
        };
        UUID mainGuid = trackToAudioRepository.findByTrackIdAndMain(trackId, true)
                .orElseThrow(() -> new NotFoundException("Main audio for track " + trackId + " not found"))
                .getGuid();

        Audio audio = audioRepository.findByGuidAndBitrate(mainGuid, bitrate)
                .orElseThrow(() -> new NotFoundException(
                        "Audio with bitrate " + bitrate + " for track " + trackId + " not found"));
        return presign(audio);
    }

    private PresignedUrlResponse presign(Audio audio) {
        String contentDisposition = ContentDisposition.attachment()
                .build().toString();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(properties.getDownload().getBucket().getName())
                .key(audio.getKey())
                .responseContentDisposition(contentDisposition)
                .responseContentType(contentType(audio.getContainer()))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(properties.getDownload().getSignatureDuration())
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        log.info("Presigned streaming URL for key [{}] expires at [{}]", audio.getKey(), presignedRequest.expiration());

        return new PresignedUrlResponse(presignedRequest.url().toExternalForm());
    }

    private static String contentType(String container) {
        if (container == null) {
            return DEFAULT_CONTENT_TYPE;
        }
        return switch (container.toLowerCase()) {
            case "ogg" -> "audio/ogg";
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "flac" -> "audio/flac";
            default -> DEFAULT_CONTENT_TYPE;
        };
    }
}
