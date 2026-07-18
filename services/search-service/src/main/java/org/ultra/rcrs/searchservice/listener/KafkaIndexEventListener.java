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
import org.ultra.rcrs.events.IndexEntityEvent;
import org.ultra.rcrs.searchservice.document.AlbumDoc;
import org.ultra.rcrs.searchservice.document.ArtistDoc;
import org.ultra.rcrs.searchservice.document.TrackDoc;
import org.ultra.rcrs.searchservice.dto.IdDto;
import org.ultra.rcrs.searchservice.service.IndexService;
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
    private final Map<String, Consumer<Object>> events = Map.of(
            IndexEntityEvent.ARTIST_CREATE_SINGLE, entityCreateSingle(ArtistDoc.class),
            IndexEntityEvent.ALBUM_CREATE_SINGLE, entityCreateSingle(AlbumDoc.class),
            IndexEntityEvent.TRACK_CREATE_SINGLE, entityCreateSingle(TrackDoc.class),
            IndexEntityEvent.ARTIST_CREATE_BATCH, entityCreateBatch(ArtistDoc.class),
            IndexEntityEvent.ALBUM_CREATE_BATCH, entityCreateBatch(AlbumDoc.class),
            IndexEntityEvent.TRACK_CREATE_BATCH, entityCreateBatch(TrackDoc.class),
            IndexEntityEvent.TRACK_DELETE, entityDelete(TrackDoc.class),
            IndexEntityEvent.ALBUM_DELETE, entityDelete(AlbumDoc.class),
            IndexEntityEvent.ARTIST_DELETE, entityDelete(ArtistDoc.class)
    );

    @KafkaListener(topics = Topics.SEARCH_INDEX_TOPIC, groupId = "my-group")
    public void handleIndexEvent(String message) {
        IndexEntityEvent event = objectMapper.readValue(message, IndexEntityEvent.class);
        log.info("Received event {}", event);
        if (event != null && !StringUtils.isEmpty(event.getEventType())) {
            if (events.containsKey(event.getEventType())) {
                events.get(event.getEventType()).accept(event.getPayload());
            }
        }
    }

    public <T> Consumer<Object> entityCreateSingle(Class<T> clazz) {
        return (payload) -> {
            T doc = objectMapper.convertValue(payload, clazz);
            IndexService<T> indexService = this.getIndexServiceForClass(clazz);
            indexService.index(doc);
        };
    }

    public <T> Consumer<Object> entityCreateBatch(Class<T> clazz) {
        return (payload) -> {

            List<T> batch = objectMapper.convertValue(payload,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            IndexService<T> indexService = this.getIndexServiceForClass(clazz);
            indexService.indexBatch(batch);
        };
    }

    public <T> Consumer<Object> entityDelete(Class<T> clazz) {
        return (payload) -> {
            IdDto idDto = objectMapper.convertValue(payload, IdDto.class);
            IndexService<T> indexService = this.getIndexServiceForClass(clazz);
            indexService.delete(idDto.id());
        };
    }

    private <T> IndexService<T> getIndexServiceForClass(Class<T> clazz) {
        ResolvableType type = ResolvableType.forClassWithGenerics(IndexService.class, clazz);
        ObjectProvider<IndexService<T>> objectProvider = context.getBeanProvider(type);
        return objectProvider.getObject();
    }


}
