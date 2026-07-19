package org.ultra.rcrs.workflowservice.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "audio-client", url = "${feign.media-service.url}")
public interface AudioClient {

}
