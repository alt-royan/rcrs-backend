package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.TrackDocument;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface TrackDocumentRepository extends ReactiveMongoRepository<TrackDocument, String> {

    Flux<TrackDocument> findAllByIdIn(List<String> ids);

    Flux<TrackDocument> findByArtistsIdAndStatusIn(String artistId, List<String> statuses, Sort sort);
}
