package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface PlaylistDocumentRepository extends ReactiveMongoRepository<PlaylistDocument, String> {

    Flux<PlaylistDocument> findAllByIdIn(List<String> ids);
}
