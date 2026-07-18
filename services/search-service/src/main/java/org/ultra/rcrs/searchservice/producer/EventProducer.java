package org.ultra.rcrs.searchservice.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.events.StartReindexEvent;
import tools.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final String reindexTopic = Topics.SEARCH_START_REINDEX_TOPIC;

    @Value("${spring.kafka.reindex.batch.size}")
    private Integer batchSize;


    public void reindexArtists() {
        var payload = objectMapper.writeValueAsString(new StartReindexEvent(EntityType.ARTIST, batchSize));
        kafkaTemplate.send(reindexTopic, payload);
    }

    public void reindexAlbums() {
        var payload = objectMapper.writeValueAsString(new StartReindexEvent(EntityType.ALBUM, batchSize));
        kafkaTemplate.send(reindexTopic, payload);
    }

    public void reindexTracks() {
        var payload = objectMapper.writeValueAsString(new StartReindexEvent(EntityType.TRACK, batchSize));
        kafkaTemplate.send(reindexTopic, payload);
    }
}
