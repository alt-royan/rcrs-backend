package org.ultra.rcrs.metadata.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface ArtistDocumentRepository extends ReactiveMongoRepository<ArtistDocument, String> {

    @Query("{ '_id': ?0 }")
    Mono<ArtistDocument> findByIdForAdmin(String id);

    @Query("{ '_id': ?0, 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Mono<ArtistDocument> findByIdForPublic(String id);

    @Query("{ '_id': { '$in': ?0 }, 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Flux<ArtistDocument> findAllByIdInForPublic(List<String> ids);
}
