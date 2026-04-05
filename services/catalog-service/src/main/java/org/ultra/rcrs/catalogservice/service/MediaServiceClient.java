package org.ultra.rcrs.catalogservice.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class MediaServiceClient {

    private final WebClient mediaWebClient;

    public MediaServiceClient(@Qualifier("mediaWebClient") WebClient mediaWebClient) {
        this.mediaWebClient = mediaWebClient;
    }

    public Mono<String> fetchImageUrl(String imageKey) {
        return mediaWebClient.get()
                .uri("/images/{imageKey}", imageKey)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new RuntimeException("Client Error: " + response.statusCode())))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new RuntimeException("Server Error: " + response.statusCode())))
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .onErrorReturn("");
    }


}
