package org.ultra.rcrs.mediaservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.Topics;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTrackEventListener {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.MEDIA_START_TRACK_TRANSCODING_TOPIC)
    public void handleTrackEvent(String payload) {
        log.info("Received message {}", payload);
    }
}
