package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.ArtistDoc;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistIndexService implements IndexService<ArtistDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(ArtistDoc doc) {
        elasticsearchOperations.save(doc);
        log.info("Artist document indexed {}", doc);
    }

    @Override
    public void delete(String id) {
        elasticsearchOperations.delete(id, ArtistDoc.class);
        log.info("Artist document deleted - id {}", id);
    }

    @Override
    public void indexBatch(List<ArtistDoc> batch) {
        elasticsearchOperations.save(batch);
        log.info("Artists batch with size {} indexed", batch.size());
    }
}
