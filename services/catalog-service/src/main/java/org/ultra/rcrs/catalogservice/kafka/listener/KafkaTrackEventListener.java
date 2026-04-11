package org.ultra.rcrs.catalogservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTrackEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic.in}")
    public void handleTrackEvent(String payload) {
        log.info("Received message {}", payload);
    }
}
