package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.ultra.rcrs.workflow.config.FeignConfig;

@FeignClient(name = "metadata-purge-client", url = "${feign.metadata-service.url}", configuration = FeignConfig.class)
public interface MetadataClient {

    @PostMapping("/admin/purge")
    ResponseEntity<Void> purge();
}
