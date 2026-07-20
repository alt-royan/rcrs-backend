package org.ultra.rcrs.searchservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.TrackDoc;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackIndexRepository implements IndexRepository<TrackDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public <D extends TrackDoc> D get(String id, Class<D> clazz) {
        return elasticsearchOperations.get(id, clazz);
    }

    @Override
    public <D extends TrackDoc> boolean exists(String id, Class<D> clazz) {
        return elasticsearchOperations.exists(id, clazz);
    }

    @Override
    public <D extends TrackDoc> void index(D doc) {
        elasticsearchOperations.save(doc);
        log.info("Track document indexed {}", doc);
    }

    @Override
    public <D extends TrackDoc> void delete(String id, Class<D> clazz) {
        elasticsearchOperations.delete(id, clazz);
        log.info("Track document deleted - id {}", id);
    }
}
