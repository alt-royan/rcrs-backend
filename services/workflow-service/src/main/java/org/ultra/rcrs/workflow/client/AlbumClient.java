package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.ultra.rcrs.workflow.client.model.AlbumUploadModel;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.dto.StatusDto;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@FeignClient(name = "album-write-client", url = "${feign.metadata-service.url}")
public interface AlbumClient {

    @PostMapping("/albums")
    ResponseEntity<CreateResponse> createAlbum(AlbumUploadModel request);

    @PostMapping("/albums/{albumId}/artists")
    ResponseEntity<Void> addArtistsToAlbum(ArtistsToEntityModel request, @PathVariable String albumId);

    @DeleteMapping("/albums/{albumId}/artists")
    ResponseEntity<Void> deleteArtistsFromAlbum(ArtistsToEntityModel request, @PathVariable String albumId);

    @PutMapping("/albums/{albumId}/status")
    ResponseEntity<Void> updateAlbumStatus(StatusDto statusDto, @PathVariable String albumId);

    @PutMapping("/albums/{albumId}/hide")
    ResponseEntity<Void> hideAlbum(@PathVariable String albumId);

    @PutMapping("/albums/{albumId}/active")
    ResponseEntity<Void> activeAlbum(@PathVariable String albumId);

    @DeleteMapping("/albums/{albumId}")
    ResponseEntity<Void> markAlbumDeleted(@PathVariable String albumId);
}
