package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.ArtistPublicDocument;
import reactor.core.publisher.Mono;

@Repository
public interface ArtistDocumentRepository extends ReactiveMongoRepository<ArtistPublicDocument, String> {

    @Query("{ '_id': ?0 }")
    Mono<ArtistPublicDocument> findByIdForAdmin(String id);

    @Query("{ '_id': ?0, 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Mono<ArtistPublicDocument> findByIdForPublic(String id);
}
