package org.ultra.rcrs.uploadservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.ultra.rcrs.uploadservice.config.FeignConfig;
import org.ultra.rcrs.uploadservice.dto.FileStatusResponse;
import org.ultra.rcrs.uploadservice.dto.PreloadFileRequest;
import org.ultra.rcrs.uploadservice.dto.S3PresignUrlResponse;

import java.util.List;
import java.util.Map;

@FeignClient(name = "media-client", url = "${feign.media-service.url}", configuration = FeignConfig.class)
public interface MediaClient {

    @PostMapping("/pre-sign")
    ResponseEntity<S3PresignUrlResponse> getPreSignUrl(@RequestBody PreloadFileRequest request);

    @PostMapping("/image")
    ResponseEntity<Object> uploadImage(@RequestBody String dataUrl);

    @GetMapping("/status")
    ResponseEntity<Map<String, FileStatusResponse>> getFilesStatus(@RequestParam(value = "uids") List<String> uids);

}