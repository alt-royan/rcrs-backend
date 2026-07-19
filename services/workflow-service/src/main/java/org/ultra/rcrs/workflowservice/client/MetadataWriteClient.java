package org.ultra.rcrs.workflowservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@FeignClient(name = "metadata-write-client", url = "${feign.metadata-service.url}")
public interface MetadataWriteClient {

    @PostMapping("/artists")
    ArtistResponse createArtist(@RequestBody CreateArtistRequest request);

    @DeleteMapping("/artists/{id}")
    ArtistResponse deleteArtist(@PathVariable("id") String id);
}
