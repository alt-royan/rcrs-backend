package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.ultra.rcrs.workflow.dto.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.ArtistsToEntityRequest;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.StatusDto;

@FeignClient(name = "album-write-client", url = "${feign.metadata-service.url}")
public interface AlbumClient {

    @PostMapping("/albums")
    ResponseEntity<CreateResponse> createAlbum(AlbumUploadRequest request);

    @PostMapping("/albums/{albumId}/artists")
    ResponseEntity<Void> addArtistsToAlbum(ArtistsToEntityRequest request, String albumId);

    @DeleteMapping("/albums/{albumId}/artists")
    ResponseEntity<Void> deleteArtistsFromAlbum(ArtistsToEntityRequest request, String albumId);

    @PutMapping("/albums/{albumId}/status")
    ResponseEntity<Void> updateAlbumStatus(StatusDto statusDto, String albumId);

    @PutMapping("/albums/{albumId}/hide")
    ResponseEntity<Void> hideAlbum(String albumId);

    @PutMapping("/albums/{albumId}/active")
    ResponseEntity<Void> activeAlbum(String albumId);

    @DeleteMapping("/albums/{albumId}")
    ResponseEntity<Void> markAlbumDeleted(String albumId);
}
