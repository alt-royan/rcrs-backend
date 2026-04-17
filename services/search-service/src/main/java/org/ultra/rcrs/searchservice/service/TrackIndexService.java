package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.TrackDoc;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackIndexService implements IndexService<TrackDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(TrackDoc doc) {
        elasticsearchOperations.save(doc);
        log.info("Track document indexed {}", doc);
    }

    @Override
    public void delete(String id) {
        elasticsearchOperations.delete(id, TrackDoc.class);
        log.info("Track document deleted - id {}", id);
    }

    @Override
    public void indexBatch(List<TrackDoc> batch) {
        elasticsearchOperations.save(batch);
        log.info("Tracks batch with size {} indexed", batch.size());
    }
}
