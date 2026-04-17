package org.ultra.rcrs.searchservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.AlbumDoc;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumIndexService implements IndexService<AlbumDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void index(AlbumDoc doc) {
        elasticsearchOperations.save(doc);
        log.info("Album document indexed {}", doc);
    }

    @Override
    public void delete(String id) {
        elasticsearchOperations.delete(id, AlbumDoc.class);
        log.info("Album document deleted - id {}", id);
    }

    @Override
    public void indexBatch(List<AlbumDoc> batch) {
        elasticsearchOperations.save(batch);
        log.info("Albums batch with size {} indexed", batch.size());
    }
}
