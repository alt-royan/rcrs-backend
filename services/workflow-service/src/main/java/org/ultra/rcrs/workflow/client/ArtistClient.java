package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@FeignClient(name = "artist-write-client", url = "${feign.metadata-service.url}")
public interface ArtistClient {

    @PostMapping("/artists")
    ResponseEntity<CreateResponse> createArtist(@RequestBody RegisterArtistRequest request);

    @DeleteMapping("/artists/{id}")
    ResponseEntity<Void> markArtistDeleted(@PathVariable("id") String id);

    @PutMapping("/artists/{id}/hide")
    ResponseEntity<Void> hideArtist(@PathVariable("id") String id);

    @PutMapping("/artists/{id}/active")
    ResponseEntity<Void> activeArtist(@PathVariable("id") String id);
}
