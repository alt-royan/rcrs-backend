package org.ultra.rcrs.catalogservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
public class MediaServiceClient {

    private final WebClient mediaWebClient;

    public MediaServiceClient(@Qualifier("mediaWebClient") WebClient mediaWebClient) {
        this.mediaWebClient = mediaWebClient;
    }

    public Mono<String> fetchImageUrl(String imageKey) {
        if (StringUtils.isEmpty(imageKey)){
            return Mono.just("");
        }
        log.info("Fetching image url...");
        return mediaWebClient.get()
                .uri("/images/{imageKey}", imageKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("Client Error: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Server Error: " + response.statusCode())))
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .timeout(Duration.ofMillis(1500))
                .onErrorReturn("");
    }


}
