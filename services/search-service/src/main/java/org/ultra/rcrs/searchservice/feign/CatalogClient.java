package org.ultra.rcrs.searchservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


@FeignClient(name = "catalog-client", url = "${feign.catalog-service.url}")
public interface CatalogClient {

    @PostMapping("/artists")
    List<Object> getArtists(@RequestBody List<String> request);

    @PostMapping("/albums")
    List<Object> getAlbums(@RequestBody List<String> request);

    @PostMapping("/tracks")
    List<Object> getTracks(@RequestBody List<String> request);
}