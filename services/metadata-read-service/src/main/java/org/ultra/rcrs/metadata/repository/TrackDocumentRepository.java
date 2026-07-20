package org.ultra.rcrs.metadata.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TrackDocumentRepository extends ReactiveMongoRepository<TrackPublicDocument, String> {

    @Query("{ '_id': ?0 }")
    Mono<TrackPublicDocument> findByIdForAdmin(String id);

    @Query("{ '_id': ?0, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Mono<TrackPublicDocument> findByIdForPublic(String id);

    @Query("{ 'album.id': ?0 }")
    Flux<TrackPublicDocument> findAllByAlbumIdForAdmin(String albumId, Sort sort);

    @Query("{ 'album.id': ?0, 'lifecycleStatus': 'PUBLISHED', 'availabilityStatus': { '$in': [ 'ACTIVE', 'HIDDEN' ] } }")
    Flux<TrackPublicDocument> findAllByAlbumIdForPublic(String albumId, Sort sort);
}
