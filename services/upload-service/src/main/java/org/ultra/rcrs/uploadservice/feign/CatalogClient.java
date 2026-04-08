package org.ultra.rcrs.uploadservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.ultra.rcrs.uploadservice.dto.AlbumUploadRequest;

@FeignClient(name = "catalog-client", url = "${feign.catalog-service.url}")
public interface CatalogClient {

    @PostMapping("/albums")
    ResponseEntity<Void> uploadAlbum(@RequestBody AlbumUploadRequest request);
}