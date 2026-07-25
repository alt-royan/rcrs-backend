package org.ultra.rcrs.playlistservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.playlistservice.model.PlaylistDocument;
import org.ultra.rcrs.playlistservice.model.PlaylistTrack;
import org.ultra.rcrs.playlistservice.repository.PlaylistDocumentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistService {

    private static final String TRACKS_FIELD = "tracks";

    private final PlaylistDocumentRepository playlistDocumentRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    public Mono<PlaylistDocument> createPlaylist(String id, String ownerId, String title, String description,
                                                  List<String> tags, List<String> trackIds, String coverS3Key,
                                                  boolean isPublic) {
        var now = LocalDateTime.now();
        List<String> distinctTrackIds = trackIds != null ? trackIds.stream().distinct().toList() : List.of();
        List<PlaylistTrack> tracks = new ArrayList<>(distinctTrackIds.size());
        for (int i = 0; i < distinctTrackIds.size(); i++) {
            tracks.add(PlaylistTrack.builder()
                    .trackId(distinctTrackIds.get(i))
                    .position(i)
                    .addedAt(now)
                    .build());
        }

        PlaylistDocument doc = PlaylistDocument.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .description(description)
                .tags(tags != null ? tags : List.of())
                .coverS3Key(coverS3Key)
                .isPublic(isPublic)
                .trackCount(tracks.size())
                .tracks(tracks)
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

    public Flux<PlaylistTrack> getTracks(String playlistId, int offset, int limit, String sortBy, Sort.Direction direction) {
        String sortField = TRACKS_FIELD + "." + ("addedAt".equalsIgnoreCase(sortBy) ? "addedAt" : "position");
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(playlistId)),
                Aggregation.unwind(TRACKS_FIELD),
                Aggregation.sort(direction, sortField),
                Aggregation.skip((long) offset),
                Aggregation.limit(limit),
                Aggregation.replaceRoot(TRACKS_FIELD)
        );
        return mongoTemplate.aggregate(aggregation, "playlists", PlaylistTrack.class);
    }

    public Mono<PlaylistDocument> addTracks(String playlistId, List<String> trackIds) {
        return playlistDocumentRepository.findById(playlistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Playlist", playlistId)))
                .flatMap(playlist -> {
                    List<PlaylistTrack> existing = playlist.getTracks() != null ? playlist.getTracks() : List.of();
                    Set<String> existingIds = existing.stream().map(PlaylistTrack::getTrackId).collect(Collectors.toSet());
                    List<String> toAdd = trackIds.stream().distinct().filter(id -> !existingIds.contains(id)).toList();
                    if (toAdd.isEmpty()) {
                        return Mono.just(playlist);
                    }

                    int startPosition = existing.size();
                    var now = LocalDateTime.now();
                    List<PlaylistTrack> newTracks = new ArrayList<>(toAdd.size());
                    for (int i = 0; i < toAdd.size(); i++) {
                        newTracks.add(PlaylistTrack.builder()
                                .trackId(toAdd.get(i))
                                .position(startPosition + i)
                                .addedAt(now)
                                .build());
                    }

                    Query query = new Query(Criteria.where("_id").is(playlistId));
                    Update update = new Update()
                            .push(TRACKS_FIELD).each(newTracks)
                            .inc("trackCount", newTracks.size())
                            .set("updatedAt", now);
                    FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                    return mongoTemplate.findAndModify(query, update, options, PlaylistDocument.class);
                })
                .doOnSuccess(d -> log.info("Added {} tracks to playlist: id={}", trackIds.size(), playlistId))
                .doOnError(e -> log.error("Failed to add tracks to playlist: id={}, error={}", playlistId, e.getMessage()));
    }

    public Mono<PlaylistDocument> deleteTracks(String playlistId, List<String> trackIds) {
        return playlistDocumentRepository.findById(playlistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Playlist", playlistId)))
                .flatMap(playlist -> {
                    List<PlaylistTrack> existing = playlist.getTracks() != null ? playlist.getTracks() : List.of();
                    List<PlaylistTrack> remaining = existing.stream()
                            .filter(t -> !trackIds.contains(t.getTrackId()))
                            .sorted(Comparator.comparingInt(PlaylistTrack::getPosition))
                            .toList();
                    if (remaining.size() == existing.size()) {
                        return Mono.just(playlist);
                    }
                    for (int i = 0; i < remaining.size(); i++) {
                        remaining.get(i).setPosition(i);
                    }

                    Query query = new Query(Criteria.where("_id").is(playlistId));
                    Update update = new Update()
                            .set(TRACKS_FIELD, remaining)
                            .set("trackCount", remaining.size())
                            .set("updatedAt", LocalDateTime.now());
                    FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
                    return mongoTemplate.findAndModify(query, update, options, PlaylistDocument.class);
                })
                .doOnSuccess(d -> log.info("Removed tracks from playlist: id={}, trackIds={}", playlistId, trackIds))
                .doOnError(e -> log.error("Failed to remove tracks from playlist: id={}, error={}", playlistId, e.getMessage()));
    }

    public Mono<Void> deletePlaylist(String playlistId) {
        return playlistDocumentRepository.deleteById(playlistId)
                .doOnSuccess(v -> log.info("Deleted playlist: id={}", playlistId))
                .doOnError(e -> log.error("Failed to delete playlist: id={}, error={}", playlistId, e.getMessage()));
    }
}
