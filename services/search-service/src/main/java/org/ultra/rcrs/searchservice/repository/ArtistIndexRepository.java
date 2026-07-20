package org.ultra.rcrs.searchservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.searchservice.document.ArtistDoc;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtistIndexRepository implements IndexRepository<ArtistDoc> {

    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public <D extends ArtistDoc> D get(String id, Class<D> clazz) {
        return elasticsearchOperations.get(id, clazz);
    }

    @Override
    public <D extends ArtistDoc> boolean exists(String id, Class<D> clazz) {
        return elasticsearchOperations.exists(id, clazz);
    }

    @Override
    public <D extends ArtistDoc> void index(D doc) {
        elasticsearchOperations.save(doc);
        log.info("Artist document indexed {}", doc);
    }

    @Override
    public <D extends ArtistDoc> void delete(String id, Class<D> clazz) {
        elasticsearchOperations.delete(id, clazz);
        log.info("Artist document deleted - id {}", id);
    }
}
