package org.ultra.rcrs.catalogservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.events.TrackCreatedEvent;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class  EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.media-service}")
    private String mediaTopic;


    public void trackCreated(String uid, UUID trackId) {
        var json = objectMapper.writeValueAsString(new TrackCreatedEvent(uid, trackId, Instant.now()));
        kafkaTemplate.send(mediaTopic, json).thenAcceptAsync(result ->
                        log.info("Sent message=[{}] with offset=[{}]", json, result.getRecordMetadata().offset()))
                .exceptionallyAsync(err -> {
                            log.info("Unable to send message=[{}] due to : {}", json, err.getMessage());
                            return null;
                        });

    }

    public void albumCreated(UUID albumId) {

    }
}
