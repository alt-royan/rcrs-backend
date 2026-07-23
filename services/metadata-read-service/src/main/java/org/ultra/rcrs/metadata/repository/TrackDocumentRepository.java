package org.ultra.rcrs.metadata.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.TrackDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TrackDocumentRepository extends ReactiveMongoRepository<TrackDocument, String> {

    @Query("{ '_id': ?0 }")
    Mono<TrackDocument> findByIdForAdmin(String id);

    @Query("{ '_id': ?0, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Mono<TrackDocument> findByIdForPublic(String id);

    @Query("{ 'album.id': ?0 }")
    Flux<TrackDocument> findAllByAlbumIdForAdmin(String albumId, Sort sort);

    @Query("{ 'album.id': ?0, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Flux<TrackDocument> findAllByAlbumIdForPublic(String albumId, Sort sort);
}
