package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@FeignClient(name = "metadata-write-client", url = "${feign.metadata-service.url}")
public interface MetadataWriteClient {

    @PostMapping("/artists")
    CreateResponse createArtist(@RequestBody RegisterArtistRequest request);

    @DeleteMapping("/artists/{id}")
    CreateResponse deleteArtist(@PathVariable("id") String id);
}
