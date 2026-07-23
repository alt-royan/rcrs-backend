package org.ultra.rcrs.mediaservice.service;

import jakarta.activation.MimetypesFileTypeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.mediaservice.config.UploadConfigurationProperties;
import org.ultra.rcrs.mediaservice.dao.model.AudioUpload;
import org.ultra.rcrs.mediaservice.dao.model.AudioWithTrack;
import org.ultra.rcrs.mediaservice.dao.repository.AudioRepository;
import org.ultra.rcrs.mediaservice.dao.repository.AudioUploadRepository;
import org.ultra.rcrs.mediaservice.dto.AudioItem;
import org.ultra.rcrs.mediaservice.dto.FileStatusResponse;
import org.ultra.rcrs.mediaservice.dto.PreloadFileRequest;
import org.ultra.rcrs.mediaservice.dto.S3PresignUrlResponse;
import org.ultra.rcrs.mediaservice.utils.Hash;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AudioService {


    private final AudioUploadRepository audioUploadRepository;
    private final AudioRepository audioRepository;
    private final S3Presigner s3Presigner;
    private final UploadConfigurationProperties uploadProperties;

    @Transactional
    public S3PresignUrlResponse getPreSignUrl(PreloadFileRequest request) {
        String key = Hash.sha1Base64(request.getName() + "_" + LocalDateTime.now());
        String contentType = (new MimetypesFileTypeMap()).getContentType(request.getName());

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(uploadProperties.getBucket().getName())
                .key(key)
                .contentLength(request.getLength())
                .contentType(contentType)
                .contentDisposition(ContentDisposition.attachment()
                        .filename(request.getName(), StandardCharsets.UTF_8)
                        .build().toString())
                .acl(ObjectCannedACL.PRIVATE)
                .serverSideEncryption(ServerSideEncryption.AES256)
                .storageClass(StorageClass.STANDARD)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(uploadProperties.getSignatureDuration())
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        var url = presignedRequest.url().toExternalForm();
        var method = presignedRequest.httpRequest().method();
        List<Map.Entry<String, String>> headers = presignedRequest.signedHeaders().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey(), entry.getValue().getFirst()))
                .toList();

        log.info("Presigned URL to upload a file to: [{}]", url);
        log.info("HTTP method: [{}]", method);
        log.info("Headers: [{}]", headers);

        audioUploadRepository.save(AudioUpload.builder()
                .uid(key)
                .status(FileStatus.WAIT_FOR_UPLOAD)
                .originalFileName(request.getName())
                .contentLength(request.getLength())
                .contentType(contentType)
                .createdAt(Instant.now())
                .error(null)
                .build());

        return S3PresignUrlResponse.builder()
                .headers(headers)
                .method(method.name())
                .uid(key)
                .url(url)
                .build();
    }

    public List<FileStatusResponse> getAudioStatus(List<String> uids) {
        List<AudioUpload> files = audioUploadRepository.findAllById(uids);
        return files.stream().map(file -> new FileStatusResponse(file.getUid(), file.getStatus(), file.getError()))
                .toList();
    }

    public Map<UUID, List<AudioItem>> getAudiosByTrackId(String trackId) {
        List<AudioWithTrack> audios = audioRepository.findAllByTrackId(trackId);
        return audios.stream()
                .map(a -> new AudioItem(a.getId(), a.getGuid(), a.getKey(), a.getCodec(), a.getContainer(),
                        a.getDurationMs(), a.getBitrate(), a.getSampleRate(), a.getByteSize(), a.getMain()))
                .collect(Collectors.groupingBy(AudioItem::getGuid));
    }

}
