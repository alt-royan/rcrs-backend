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

    private final TrackToAudioRepository trackToAudioRepository;
    private final AudioRepository audioRepository;
    private final AudioConfigurationProperties audioProperties;
    private final S3Presigner s3Presigner;

    public PresignedUrlResponse streamTrack(String trackId, Quality quality) {
        UUID mainGuid = trackToAudioRepository.findByTrackIdAndMain(trackId, true)
                .orElseThrow(() -> new NotFoundException("Main audio for track " + trackId + " not found"))
                .getGuid();

        Audio audio = audioRepository.findByGuidAndQuality(mainGuid, quality)
                .orElseThrow(() -> new NotFoundException(
                        "Audio with quality " + quality + " for track " + trackId + " not found"));

        return presign(audio);
    }

    private PresignedUrlResponse presign(Audio audio) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(audioProperties.getBucket().getName())
                .key(audio.getKey())
                .responseContentType(audio.getContentType())
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(audioProperties.getSignatureDuration())
                .getObjectRequest(objectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        log.info("Presigned streaming URL for key [{}] expires at [{}]", audio.getKey(), presignedRequest.expiration());

        return new PresignedUrlResponse(presignedRequest.url().toExternalForm());
    }

}
