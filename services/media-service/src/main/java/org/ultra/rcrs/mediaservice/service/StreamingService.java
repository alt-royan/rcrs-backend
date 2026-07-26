package org.ultra.rcrs.mediaservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.mediaservice.config.AudioConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.Audio;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.TrackToAudioRepository;
import org.ultra.rcrs.mediaservice.dto.PresignedUrlResponse;
import org.ultra.rcrs.mediaservice.enums.Quality;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final TrackToAudioRepository trackToAudioRepository;
    private final AudioRepository audioRepository;
    private final AudioConfigurationProperties audioProperties;
    private final S3Presigner s3Presigner;

    public PresignedUrlResponse streamTrack(String trackId, Quality quality) {
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
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(audioProperties.getBucket().getName())
                .key(audio.getKey())
                .responseContentType(contentType(audio.getContainer()))
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(audioProperties.getSignatureDuration())
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
