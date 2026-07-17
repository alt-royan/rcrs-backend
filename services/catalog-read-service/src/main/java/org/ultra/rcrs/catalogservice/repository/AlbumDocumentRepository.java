package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.AlbumDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface AlbumDocumentRepository extends ReactiveMongoRepository<AlbumDocument, String> {

    Flux<AlbumDocument> findAllByIdIn(List<String> ids);

    Flux<AlbumDocument> findByArtistsIdAndStatusIn(String artistId, List<String> statuses, Sort sort);

    Mono<Void> deleteById(String id);
}
