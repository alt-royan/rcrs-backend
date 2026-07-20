package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.client.model.ArtistCreateModel;

@FeignClient(name = "artist-write-client", url = "${feign.metadata-service.url}")
public interface ArtistClient {

    @PostMapping("/artists")
    ResponseEntity<CreateResponse> createArtist(@RequestBody ArtistCreateModel request);

    @DeleteMapping("/artists/{id}")
    ResponseEntity<Void> markArtistDeleted(@PathVariable("id") String id);

    @PutMapping("/artists/{id}/hide")
    ResponseEntity<Void> hideArtist(@PathVariable("id") String id);

    @PutMapping("/artists/{id}/active")
    ResponseEntity<Void> activeArtist(@PathVariable("id") String id);
}
