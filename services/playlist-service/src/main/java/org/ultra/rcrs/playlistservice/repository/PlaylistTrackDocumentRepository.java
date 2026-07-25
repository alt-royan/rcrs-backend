package org.ultra.rcrs.playlistservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.playlistservice.model.PlaylistTrackDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface PlaylistTrackDocumentRepository extends ReactiveMongoRepository<PlaylistTrackDocument, String> {

    Mono<Long> countByPlaylistId(String playlistId);

    Mono<Long> deleteByPlaylistIdAndTrackIdIn(String playlistId, List<String> trackIds);

    Mono<Long> deleteByPlaylistId(String playlistId);

    Flux<PlaylistTrackDocument> findAllByPlaylistIdAndTrackIdIn(String playlistId, List<String> trackIds);
}
