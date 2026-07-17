package org.ultra.rcrs.catalogservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.IndexEntityEvent;
import org.ultra.rcrs.kafka.events.StartTrackTranscodingEvent;
import org.ultra.rcrs.kafka.events.UpdateEntityStatusEvent;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final tools.jackson.databind.ObjectMapper objectMapper;

    public void trackCreated(String uid, UUID trackId) {
        sendSearchIndexEvent(IndexEntityEvent.TRACK_CREATE_SINGLE, Map.of("id", Url62.encode(trackId)));
        sendEvent(Topics.MEDIA_START_TRACK_TRANSCODING_TOPIC,
                new StartTrackTranscodingEvent(uid, Url62.encode(trackId), Instant.now()));
        sendEvent(Topics.CATALOG_UPDATE_ENTITY_STATUS_TOPIC,
                new UpdateEntityStatusEvent(Url62.encode(trackId), EntityType.TRACK, LifecycleStatus.TRANSCODING));
    }

    public void trackDeleted(UUID trackId) {
        sendSearchIndexEvent(IndexEntityEvent.TRACK_DELETE, Map.of("id", Url62.encode(trackId)));
    }

    public void albumDeleted(UUID albumId) {
        sendSearchIndexEvent(IndexEntityEvent.ALBUM_DELETE, Map.of("id", Url62.encode(albumId)));
    }

    public void albumCreated(UUID albumId) {
        sendSearchIndexEvent(IndexEntityEvent.ALBUM_CREATE_SINGLE, Map.of("id", Url62.encode(albumId)));
    }

    public void artistCreated(UUID artistId) {
        sendSearchIndexEvent(IndexEntityEvent.ARTIST_CREATE_SINGLE, Map.of("id", Url62.encode(artistId)));
    }

    private void sendSearchIndexEvent(String eventType, Object payload) {
        sendEvent(Topics.SEARCH_INDEX_TOPIC, new IndexEntityEvent(eventType, payload));
    }

    private void sendEvent(String topic, Object event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, UUID.randomUUID().toString(), json);
            future.thenAcceptAsync(result ->
                    log.info("Sent message=[{}] to topic=[{}] with offset=[{}]",
                            result.getProducerRecord().value(),
                            result.getProducerRecord().topic(),
                            result.getRecordMetadata().offset()))
                    .exceptionallyAsync(err -> {
                        log.error("Unable to send message due to: {}", err.getMessage());
                        return null;
                    });
        } catch (Exception e) {
            log.error("Failed to serialize/send event: {}", e.getMessage(), e);
        }
    }
}
