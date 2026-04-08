package org.ultra.rcrs.uploadservice.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.ServiceUnavailableException;
import org.ultra.rcrs.uploadservice.dto.AlbumUploadRequest;
import org.ultra.rcrs.uploadservice.dto.PreloadFileRequest;
import org.ultra.rcrs.uploadservice.feign.CatalogClient;
import org.ultra.rcrs.uploadservice.feign.MediaClient;

import java.util.List;

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

    public ResponseEntity<Object> uploadImage(String dataUrl) {
        try {
            var response = mediaClient.uploadImage(dataUrl);
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
