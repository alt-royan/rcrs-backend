package org.ultra.rcrs.mediaservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.UpdateEntityStatusEvent;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void startTranscoding(String trackId) {
        updateTrackStatus(trackId, EntityStatus.TRANSCODING);
    }

    public void failedTranscoding(String trackId) {
        updateTrackStatus(trackId, EntityStatus.FAILED);
    }

    public void successTranscoding(String trackId) {
        updateTrackStatus(trackId, EntityStatus.READY);
    }

    public void updateTrackStatus(String trackId, EntityStatus status) {
        String event = objectMapper.writeValueAsString(new UpdateEntityStatusEvent(trackId, EntityType.TRACK, status));
        var future = kafkaTemplate.send(Topics.CATALOG_UPDATE_ENTITY_STATUS_TOPIC, UUID.randomUUID().toString(), event);
        log(future);
    }

    private void log(CompletableFuture<SendResult<String, String>> future) {
        future.thenAcceptAsync(result ->
                        log.info("Sent message=[{}] to topic=[{}] with offset=[{}]", result.getProducerRecord().value(),
                                result.getProducerRecord().topic(), result.getRecordMetadata().offset()))
                .exceptionallyAsync(err -> {
                    log.info("Unable to send message due to : {}", err.getMessage());
                    return null;
                });
    }
}
