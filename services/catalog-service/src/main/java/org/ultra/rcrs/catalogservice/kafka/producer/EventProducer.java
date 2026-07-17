package org.ultra.rcrs.catalogservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.service.IndexService;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.IndexEntityEvent;
import org.ultra.rcrs.kafka.events.StartTrackTranscodingEvent;
import org.ultra.rcrs.kafka.events.UpdateEntityStatusEvent;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IndexService indexService;

    public Mono<Void> trackCreated(String uid, UUID trackId) {
        return indexService.createTrackIndexEvent(trackId)
                .map(objectMapper::writeValueAsString)
                .map(event -> kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, UUID.randomUUID().toString(), event))
                .doOnNext(this::log)
                .then(Mono.just(new StartTrackTranscodingEvent(uid, Url62.encode(trackId), Instant.now()))
                        .map(objectMapper::writeValueAsString)
                        .map(event -> kafkaTemplate.send(Topics.MEDIA_START_TRACK_TRANSCODING_TOPIC, event))
                        .doOnNext(this::log)
                ).then(Mono.just(new UpdateEntityStatusEvent(Url62.encode(trackId), EntityType.TRACK, LifecycleStatus.TRANSCODING))
                        .map(objectMapper::writeValueAsString)
                        .map(event -> kafkaTemplate.send(Topics.CATALOG_UPDATE_ENTITY_STATUS_TOPIC, event))
                        .doOnNext(this::log)
                        .then());
    }

    public Mono<Void> trackDeleted(UUID trackId) {
        return indexService.deleteIndexEvent(trackId, IndexEntityEvent.TRACK_DELETE)
                .map(objectMapper::writeValueAsString)
                .map(event -> kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, UUID.randomUUID().toString(), event))
                .doOnNext(this::log)
                .then();
    }

    public Mono<Void> albumDeleted(UUID albumId) {
        return indexService.deleteIndexEvent(albumId, IndexEntityEvent.ALBUM_DELETE)
                .map(objectMapper::writeValueAsString)
                .map(event -> kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, UUID.randomUUID().toString(), event))
                .doOnNext(this::log)
                .then();
    }

    public Mono<Void> albumCreated(UUID albumId) {
        return indexService.createAlbumIndexEvent(albumId)
                .map(objectMapper::writeValueAsString)
                .map(event -> kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, UUID.randomUUID().toString(), event))
                .doOnNext(this::log)
                .then();
    }

    public Mono<Void> artistCreated(UUID artistId) {
        return indexService.createArtistIndexEvent(artistId)
                .map(objectMapper::writeValueAsString)
                .map(event -> kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, UUID.randomUUID().toString(), event))
                .doOnNext(this::log)
                .then();
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
