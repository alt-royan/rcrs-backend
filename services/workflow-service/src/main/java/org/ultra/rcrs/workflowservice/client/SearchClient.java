package org.ultra.rcrs.workflowservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "search-client", url = "${feign.search-service.url}")
public interface SearchClient {

    @PostMapping("/api/search/index")
    void indexEntity(@RequestParam("entityType") String entityType, @RequestParam("entityId") String entityId);

    @DeleteMapping("/api/search/index")
    void removeEntity(@RequestParam("entityType") String entityType, @RequestParam("entityId") String entityId);

    @PostMapping("/api/search/reindex")
    void reindex(@RequestParam("entityType") String entityType);
}
