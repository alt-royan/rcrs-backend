package org.ultra.rcrs.uploadservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.ultra.rcrs.uploadservice.dto.AlbumUploadRequest;
import org.ultra.rcrs.uploadservice.dto.ArtistCreateRequest;
import org.ultra.rcrs.uploadservice.dto.IdResponse;

@FeignClient(name = "catalog-client", url = "${feign.catalog-service.url}")
public interface CatalogClient {

    @PostMapping("/albums")
    ResponseEntity<IdResponse> uploadAlbum(@RequestBody AlbumUploadRequest request);

    @PostMapping("/artists")
    ResponseEntity<IdResponse> createArtist(@RequestBody ArtistCreateRequest request);
}