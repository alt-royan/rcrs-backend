package org.ultra.rcrs.metadata.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AlbumDocumentRepository extends ReactiveMongoRepository<AlbumDocument, String> {

    @Query("{ '_id': ?0 }")
    Mono<AlbumDocument> findByIdForAdmin(String id);

    @Query("{ '_id': ?0, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Mono<AlbumDocument> findByIdForPublic(String id);

    @Query("{ '_id': { '$in': ?0 }, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Flux<AlbumDocument> findAllByIdInForPublic(List<String> ids);
}
