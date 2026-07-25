package org.ultra.rcrs.playlistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;
import org.ultra.rcrs.playlistservice.model.PlaylistTrackDocument;
import org.ultra.rcrs.playlistservice.repository.PlaylistDocumentRepository;
import org.ultra.rcrs.playlistservice.repository.PlaylistTrackDocumentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

    private final PlaylistDocumentRepository playlistDocumentRepository;
    private final PlaylistTrackDocumentRepository playlistTrackDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<PlaylistDocument> createPlaylist(String id, String ownerId, String title,
                                                  String description, String coverS3Key, boolean isPublic) {
        var now = LocalDateTime.now();
        PlaylistDocument doc = PlaylistDocument.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .description(description)
                .coverS3Key(coverS3Key)
                .isPublic(isPublic)
                .trackCount(0)
                .nextPosition(0)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return playlistDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Created playlist: id={}, ownerId={}", d.getId(), d.getOwnerId()))
                .doOnError(e -> log.error("Failed to create playlist: id={}, error={}", id, e.getMessage()));
    }

    public Flux<PlaylistDocument> getPlaylistsByIds(List<String> ids) {
        return playlistDocumentRepository.findAllByIdIn(ids);
    }

    public Flux<PlaylistTrackDocument> getTracks(String playlistId, int offset, int limit) {
        Query query = new Query(Criteria.where("playlistId").is(playlistId))
                .with(Sort.by(Sort.Direction.ASC, "position"))
                .skip(offset)
                .limit(limit);
        return mongoTemplate.find(query, PlaylistTrackDocument.class, "playlist_tracks");
    }

    public Mono<PlaylistDocument> addTracks(String playlistId, List<String> trackIds) {
        return playlistDocumentRepository.findById(playlistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Playlist", playlistId)))
                .flatMap(playlist -> reservePositions(playlistId, trackIds.size())
                        .flatMap(startPosition -> {
                            var now = LocalDateTime.now();
                            List<PlaylistTrackDocument> newTracks = new ArrayList<>();
                            for (int i = 0; i < trackIds.size(); i++) {
                                newTracks.add(PlaylistTrackDocument.builder()
                                        .playlistId(playlistId)
                                        .trackId(trackIds.get(i))
                                        .position(startPosition + i)
                                        .addedAt(now)
                                        .build());
                            }
                            return playlistTrackDocumentRepository.saveAll(newTracks).then();
                        })
                        .then(incrementTrackCount(playlistId, trackIds.size())))
                .doOnSuccess(d -> log.info("Added {} tracks to playlist: id={}", trackIds.size(), playlistId))
                .doOnError(e -> log.error("Failed to add tracks to playlist: id={}, error={}", playlistId, e.getMessage()));
    }

    public Mono<PlaylistDocument> deleteTracks(String playlistId, List<String> trackIds) {
        return playlistDocumentRepository.findById(playlistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Playlist", playlistId)))
                .flatMap(playlist -> playlistTrackDocumentRepository.deleteByPlaylistIdAndTrackIdIn(playlistId, trackIds)
                        .flatMap(deletedCount -> incrementTrackCount(playlistId, (int) -deletedCount)))
                .doOnSuccess(d -> log.info("Removed tracks from playlist: id={}, trackIds={}", playlistId, trackIds))
                .doOnError(e -> log.error("Failed to remove tracks from playlist: id={}, error={}", playlistId, e.getMessage()));
    }

    public Mono<Void> deletePlaylist(String playlistId) {
        return playlistTrackDocumentRepository.deleteByPlaylistId(playlistId)
                .then(playlistDocumentRepository.deleteById(playlistId))
                .doOnSuccess(v -> log.info("Deleted playlist: id={}", playlistId))
                .doOnError(e -> log.error("Failed to delete playlist: id={}, error={}", playlistId, e.getMessage()));
    }

    private Mono<Integer> reservePositions(String playlistId, int count) {
        Query query = new Query(Criteria.where("_id").is(playlistId));
        Update update = new Update().inc("nextPosition", count);
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(false);
        return mongoTemplate.findAndModify(query, update, options, PlaylistDocument.class)
                .map(PlaylistDocument::getNextPosition);
    }

    private Mono<PlaylistDocument> incrementTrackCount(String playlistId, int delta) {
        Query query = new Query(Criteria.where("_id").is(playlistId));
        Update update = new Update().inc("trackCount", delta).set("updatedAt", LocalDateTime.now());
        FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
        return mongoTemplate.findAndModify(query, update, options, PlaylistDocument.class);
    }
}
