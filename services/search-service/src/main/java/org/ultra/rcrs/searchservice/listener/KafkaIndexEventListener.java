package org.ultra.rcrs.searchservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.kafka.events.IndexEntityEvent;
import org.ultra.rcrs.searchservice.document.AlbumDoc;
import org.ultra.rcrs.searchservice.document.ArtistDoc;
import org.ultra.rcrs.searchservice.document.TrackDoc;
import org.ultra.rcrs.searchservice.service.IndexService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaIndexEventListener {

    private final ObjectMapper objectMapper;
    private final ApplicationContext context;
    private final Map<String, Consumer<String>> events = Map.of(
            IndexEntityEvent.ARTIST_CREATE_SINGLE, entityCreateSingle(ArtistDoc.class),
            IndexEntityEvent.ALBUM_CREATE_SINGLE, entityCreateSingle(AlbumDoc.class),
            IndexEntityEvent.TRACK_CREATE_SINGLE, entityCreateSingle(TrackDoc.class),
            IndexEntityEvent.ARTIST_CREATE_BATCH, entityCreateBatch(ArtistDoc.class),
            IndexEntityEvent.ALBUM_CREATE_BATCH, entityCreateBatch(AlbumDoc.class),
            IndexEntityEvent.TRACK_CREATE_BATCH, entityCreateBatch(TrackDoc.class)
    );

    @KafkaListener(topics = Topics.SEARCH_INDEX_TOPIC)
    public void handleIndexEvent(IndexEntityEvent event) {
        log.info("Received event {}", event);
        if (event != null && !StringUtils.isEmpty(event.getEventType())) {
            if (events.containsKey(event.getEventType())) {
                events.get(event.getEventType()).accept(event.getPayload());
            }
        }
    }

    public <T> Consumer<String> entityCreateSingle(Class<T> clazz) {
        return (payload) -> {
            T doc = objectMapper.readValue(payload, clazz);
            IndexService<T> indexService = this.getIndexServiceForClass(clazz);
            indexService.index(doc);
        };
    }

    public <T> Consumer<String> entityCreateBatch(Class<T> clazz) {
        return (payload) -> {

            List<T> batch = objectMapper.readValue(payload, new TypeReference<>() {
            });
            IndexService<T> indexService = this.getIndexServiceForClass(clazz);
            indexService.indexBatch(batch);
        };
    }

    private <T> IndexService<T> getIndexServiceForClass(Class<T> clazz) {
        ResolvableType type = ResolvableType.forClassWithGenerics(IndexService.class, clazz);
        ObjectProvider<IndexService<T>> objectProvider = context.getBeanProvider(type);
        return objectProvider.getObject();
    }


}
