package org.ultra.rcrs.uploadservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.FileStatus;
import org.ultra.rcrs.exceptions.BadRequestException;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.uploadservice.dto.AlbumUploadRequest;
import org.ultra.rcrs.uploadservice.dto.ImageUploadRequest;
import org.ultra.rcrs.uploadservice.dto.PreloadFileRequest;
import org.ultra.rcrs.uploadservice.dto.TrackUploadRequest;
import org.ultra.rcrs.uploadservice.feign.CatalogClient;
import org.ultra.rcrs.uploadservice.feign.MediaClient;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadService {

    private final MediaClient mediaClient;
    private final CatalogClient catalogClient;

    public ResponseEntity<Object> preSigned(PreloadFileRequest request) {
        try {
            var response = mediaClient.getPreSignUrl(request);
            return ResponseEntity.ok(response.getBody());
        } catch (FeignException e) {
            if (e.status() == -1) {
                throw new ServiceUnavailableException(e);
            }
            log.info(e.getMessage(), e);
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }

    }

    public ResponseEntity<Object> uploadAlbum(AlbumUploadRequest request) {
        try {
            if (request.getTracks() != null && !request.getTracks().isEmpty()) {
                List<String> uids = request.getTracks().stream().map(TrackUploadRequest::getUid).toList();
                var statuses = mediaClient.getFilesStatus(uids).getBody();
                if (statuses == null){
                    throw new BadRequestException("Incorrect file UIDs or not all files have been uploaded yet. Check and try again.");
                }
                for (var uid : uids){
                    statuses.stream()
                            .filter(s -> Objects.equals(s.getUid(), uid) &&  FileStatus.UPLOADED.equals(s.getStatus()))
                            .findAny().orElseThrow(() ->new BadRequestException("Incorrect file UIDs or not all files have been uploaded yet. Check and try again."));
                }
            }

            var response = catalogClient.uploadAlbum(request);
            return ResponseEntity.ok(response.getBody());
        } catch (FeignException e) {
            if (e.status() == -1) {
                throw new ServiceUnavailableException(e);
            }
            log.info(e.getMessage(), e);
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    public ResponseEntity<Object> uploadImage(ImageUploadRequest request) {
        try {
            var response = mediaClient.uploadImage(request);
            return ResponseEntity.ok(response.getBody());
        } catch (FeignException e) {
            if (e.status() == -1) {
                throw new ServiceUnavailableException(e);
            }
            log.info(e.getMessage(), e);
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }

    public ResponseEntity<Object> getFilesStatus(List<String> uids) {
        try {
            var response = mediaClient.getFilesStatus(uids);
            return ResponseEntity.ok(response.getBody());
        } catch (FeignException e) {
            if (e.status() == -1) {
                throw new ServiceUnavailableException(e);
            }
            log.info(e.getMessage(), e);
            return ResponseEntity.status(e.status()).body(e.contentUTF8());
        }
    }
}
