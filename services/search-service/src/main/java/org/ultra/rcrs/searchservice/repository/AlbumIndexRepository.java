package org.ultra.rcrs.searchservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.AlbumDoc;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlbumIndexRepository implements IndexRepository<AlbumDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public <D extends AlbumDoc> D get(String id, Class<D> clazz) {
        return elasticsearchOperations.get(id, clazz);
    }

    @Override
    public <D extends AlbumDoc> boolean exists(String id, Class<D> clazz) {
        return elasticsearchOperations.exists(id, clazz);
    }

    @Override
    public <D extends AlbumDoc> void index(D doc) {
        elasticsearchOperations.save(doc);
        log.info("Album document indexed {}", doc);
    }

    @Override
    public <D extends AlbumDoc> void delete(String id, Class<D> clazz) {
        elasticsearchOperations.delete(id, clazz);
        log.info("Album document deleted - id {}", id);
    }
}
