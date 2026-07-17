package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.ArtistDocument;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface ArtistDocumentRepository extends ReactiveMongoRepository<ArtistDocument, String> {

    Flux<ArtistDocument> findAllByIdIn(List<String> ids);
}
