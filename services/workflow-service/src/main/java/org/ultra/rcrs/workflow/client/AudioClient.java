package org.ultra.rcrs.workflow.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "audio-client", url = "${feign.media-service.url}")
public interface AudioClient {

}
