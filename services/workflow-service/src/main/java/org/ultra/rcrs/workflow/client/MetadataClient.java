package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "metadata-purge-client", url = "${feign.metadata-service.url}")
public interface MetadataClient {

    @PostMapping("/admin/purge")
    ResponseEntity<Void> purge();
}
