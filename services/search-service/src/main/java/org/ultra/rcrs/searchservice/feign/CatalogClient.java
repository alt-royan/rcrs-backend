package org.ultra.rcrs.searchservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "catalog-client", url = "${feign.catalog-service.url}")
public interface CatalogClient {

    @PostMapping("/metadata/artists/get")
    List<Object> getArtists(@RequestBody List<String> request);

    @PostMapping("/metadata/albums/get")
    List<Object> getAlbums(@RequestBody List<String> request);

    @PostMapping("/metadata/tracks/get")
    List<Object> getTracks(@RequestBody List<String> request);
}