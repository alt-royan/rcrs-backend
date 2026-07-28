package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.ultra.rcrs.workflow.config.FeignMediaServiceConfig;
import org.ultra.rcrs.workflow.dto.AudioStatusResponse;

import java.util.List;

@FeignClient(name = "audio-client", url = "${feign.media-service.url}", configuration = FeignMediaServiceConfig.class)
public interface AudioClient {

    @PostMapping("/media/upload/audio/get-status")
    ResponseEntity<List<AudioStatusResponse>> getAudioStatus(@RequestParam("uids") List<String> uids);
}
