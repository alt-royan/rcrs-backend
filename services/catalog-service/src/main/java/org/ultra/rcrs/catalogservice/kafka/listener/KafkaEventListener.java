package org.ultra.rcrs.catalogservice.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.catalogservice.service.IndexService;
import org.ultra.rcrs.catalogservice.service.StatusService;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.StartReindexEvent;
import org.ultra.rcrs.kafka.events.UpdateEntityStatusEvent;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IndexService indexService;
    private final StatusService statusService;

    @KafkaListener(topics = Topics.CATALOG_UPDATE_ENTITY_STATUS_TOPIC, groupId = "my-group")
    public void handleUpdateStatusEvent(String message) {
        log.info("Received message {}", message);
        UpdateEntityStatusEvent event = objectMapper.readValue(message, UpdateEntityStatusEvent.class);
        var status = event.getNewStatus();
        if (EntityType.TRACK.equals(event.getEntityType()) && !StringUtils.isEmpty(event.getId())) {
            statusService.updateTrackStatus(Url62.decode(event.getId()), status).subscribe();
        }
    }

    @KafkaListener(topics = Topics.SEARCH_START_REINDEX_TOPIC, groupId = "my-group")
    public void handleReindexEvent(String message) {
        log.info("Received message {}", message);
        StartReindexEvent event = objectMapper.readValue(message, StartReindexEvent.class);
        if (event != null && event.getEntityType() != null && event.getBatchSize() > 0) {
            AtomicReference<Mono<Void>> mono = new AtomicReference<>(Mono.empty());
            if (event.getEntityType() == EntityType.ARTIST) {
                mono.set(reindexArtists(event.getBatchSize()));
            } else if (event.getEntityType() == EntityType.ALBUM) {
                mono.set(reindexAlbums(event.getBatchSize()));
            } else if (event.getEntityType() == EntityType.TRACK) {
                mono.set(reindexTracks(event.getBatchSize()));
            }
            CompletableFuture.runAsync(() -> mono.get().subscribe());
        }
    }

    private Mono<Void> reindexArtists(int batchSize) {
        return indexService.createArtistIndexEvents(batchSize)
                .map(objectMapper::writeValueAsString)
                .doOnNext(data -> {
                    kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, data);
                    log.info("Send Artists batch to topic {} data: {}", Topics.SEARCH_INDEX_TOPIC, data);
                })
                .then();
    }

    private Mono<Void> reindexAlbums(int batchSize) {
        return indexService.createAlbumIndexEvents(batchSize)
                .map(objectMapper::writeValueAsString)
                .doOnNext(data -> {
                    kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, data);
                    log.info("Send Albums batch to topic {} data: {}", Topics.SEARCH_INDEX_TOPIC, data);
                })
                .then();
    }

    private Mono<Void> reindexTracks(int batchSize) {
        return indexService.createTrackIndexEvents(batchSize)
                .map(objectMapper::writeValueAsString)
                .doOnNext(data -> {
                    kafkaTemplate.send(Topics.SEARCH_INDEX_TOPIC, data);
                    log.info("Send Tracks batch to topic {} data: {}", Topics.SEARCH_INDEX_TOPIC, data);
                })
                .then();
    }

}
