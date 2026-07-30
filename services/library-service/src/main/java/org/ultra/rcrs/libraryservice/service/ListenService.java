package org.ultra.rcrs.libraryservice.service;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.libraryservice.dto.request.ListenReportRequest;
import org.ultra.rcrs.libraryservice.dto.response.*;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.PlaybackSource;
import org.ultra.rcrs.libraryservice.model.mongo.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ingests client-reported playbacks and serves everything derived from them.
 * <p>
 * Plays are reported by the client rather than observed server-side because
 * media-service hands out presigned S3 URLs and never sees the bytes flow: it
 * cannot know how much of a track was played, whether it finished, or what the
 * user was playing from. Client reporting is also the only thing that makes
 * offline playback countable. The trade is that a client can inflate its own
 * counts — acceptable while these numbers only feed the user's own library.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ListenService {

    /** Fraction of a track that must be played for it to count as completed. */
    private static final double COMPLETION_THRESHOLD = 0.9;

    private static final int DUPLICATE_KEY_ERROR = 11000;

    private final MongoTemplate mongoTemplate;

    @Value("${library.recently-played.max-entries:200}")
    private int recentlyPlayedMaxEntries;

    /**
     * Records a batch of playbacks and folds them into the rollups.
     * <p>
     * The raw insert and the rollup updates are deliberately not wrapped in a
     * multi-document transaction: a crash between them costs at most one lost
     * count increment, and requiring a replica-set transaction to avoid that would
     * buy nothing for this kind of data.
     */
    public ListenIngestResponse ingest(String userId, List<ListenReportRequest> reports) {
        if (reports.isEmpty()) {
            return new ListenIngestResponse(0, 0);
        }

        List<ListenEvent> documents = reports.stream()
                .map(report -> toDocument(userId, report))
                .toList();

        Set<Integer> rejected = insertIgnoringDuplicates(documents);

        List<ListenEvent> accepted = new ArrayList<>();
        for (int i = 0; i < documents.size(); i++) {
            if (!rejected.contains(i)) {
                accepted.add(documents.get(i));
            }
        }

        if (!accepted.isEmpty()) {
            updatePlayCounts(userId, accepted);
            updateRecentlyPlayed(userId, accepted);
            trimRecentlyPlayed(userId);
        }

        log.debug("Ingested {} listens for user {} ({} duplicates)", accepted.size(), userId, rejected.size());
        return new ListenIngestResponse(accepted.size(), rejected.size());
    }

    /**
     * @return indexes within {@code documents} that were rejected as duplicates
     */
    private Set<Integer> insertIgnoringDuplicates(List<ListenEvent> documents) {
        try {
            BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, ListenEvent.class);
            bulk.insert(documents);
            bulk.execute();
            return Set.of();
        } catch (org.springframework.data.mongodb.BulkOperationException ex) {
            Set<Integer> duplicates = new HashSet<>();
            for (BulkWriteError error : ex.getErrors()) {
                if (error.getCode() == DUPLICATE_KEY_ERROR) {
                    duplicates.add(error.getIndex());
                } else {
                    log.error("Listen ingest write error at index {}: {}", error.getIndex(), error.getMessage());
                    throw ex;
                }
            }
            return duplicates;
        } catch (MongoBulkWriteException ex) {
            Set<Integer> duplicates = new HashSet<>();
            for (BulkWriteError error : ex.getWriteErrors()) {
                if (error.getCode() == DUPLICATE_KEY_ERROR) {
                    duplicates.add(error.getIndex());
                } else {
                    throw ex;
                }
            }
            return duplicates;
        }
    }

    private void updatePlayCounts(String userId, List<ListenEvent> accepted) {
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, PlayCount.class);
        for (ListenEvent event : accepted) {
            Query query = new Query(Criteria.where("_id").is(new PlayCountId(userId, event.getTrackId())));
            Update update = new Update()
                    .inc("playCount", 1)
                    .inc("msPlayed", event.getMsPlayed())
                    .max("lastPlayedAt", event.getPlayedAt())
                    .min("firstPlayedAt", event.getPlayedAt());
            bulk.upsert(query, update);
        }
        bulk.execute();
    }

    /**
     * Attributes each play to the thing the user was browsing when they started it,
     * falling back to the track itself when the source is not a browsable entity.
     */
    private void updateRecentlyPlayed(String userId, List<ListenEvent> accepted) {
        BulkOperations bulk = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, RecentlyPlayed.class);
        for (ListenEvent event : accepted) {
            RecentlyPlayedId id = attribution(userId, event);
            Query query = new Query(Criteria.where("_id").is(id));
            Update update = new Update()
                    .inc("playCount", 1)
                    .max("lastPlayedAt", event.getPlayedAt());
            bulk.upsert(query, update);
        }
        bulk.execute();
    }

    private RecentlyPlayedId attribution(String userId, ListenEvent event) {
        PlaybackSource source = event.getSourceType();
        LibraryEntityType type = source == null ? null : source.toEntityType();
        if (type != null && event.getSourceId() != null) {
            return new RecentlyPlayedId(userId, type, event.getSourceId());
        }
        return new RecentlyPlayedId(userId, LibraryEntityType.TRACK, event.getTrackId());
    }

    /** Keeps the shelf bounded; it is a display surface, not a record. */
    private void trimRecentlyPlayed(String userId) {
        Query byUser = new Query(Criteria.where("_id.userId").is(userId));
        long total = mongoTemplate.count(byUser, RecentlyPlayed.class);
        if (total <= recentlyPlayedMaxEntries) {
            return;
        }

        Query cutoffQuery = new Query(Criteria.where("_id.userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "lastPlayedAt"))
                .skip(recentlyPlayedMaxEntries - 1L)
                .limit(1);
        RecentlyPlayed cutoff = mongoTemplate.findOne(cutoffQuery, RecentlyPlayed.class);
        if (cutoff == null) {
            return;
        }
        mongoTemplate.remove(new Query(Criteria.where("_id.userId").is(userId)
                .and("lastPlayedAt").lt(cutoff.getLastPlayedAt())), RecentlyPlayed.class);
    }

    public ListenTimelineResponse timeline(String userId, Instant cursor, int limit) {
        Criteria criteria = Criteria.where("userId").is(userId);
        if (cursor != null) {
            criteria = criteria.and("playedAt").lt(cursor);
        }
        Query query = new Query(criteria)
                .with(Sort.by(Sort.Direction.DESC, "playedAt"))
                .limit(limit);

        List<ListenEvent> events = mongoTemplate.find(query, ListenEvent.class);
        List<ListenEventDto> items = events.stream()
                .map(event -> new ListenEventDto(event.getTrackId(), event.getAlbumId(), event.getPlayedAt(),
                        event.getMsPlayed(), event.isCompleted(), event.getSourceType(), event.getSourceId()))
                .toList();

        // A short page means there is nothing older to fetch.
        Instant nextCursor = events.size() < limit ? null : events.get(events.size() - 1).getPlayedAt();
        return new ListenTimelineResponse(items, nextCursor);
    }

    public List<RecentlyPlayedDto> recentlyPlayed(String userId, LibraryEntityType type, int limit) {
        Criteria criteria = Criteria.where("_id.userId").is(userId);
        if (type != null) {
            criteria = criteria.and("_id.entityType").is(type.name());
        }
        Query query = new Query(criteria).with(Sort.by(Sort.Direction.DESC, "lastPlayedAt")).limit(limit);

        return mongoTemplate.find(query, RecentlyPlayed.class).stream()
                .map(entry -> new RecentlyPlayedDto(entry.getId().getEntityType(), entry.getId().getEntityId(),
                        entry.getLastPlayedAt(), entry.getPlayCount()))
                .toList();
    }

    public List<TrackPlayCountDto> topTracks(String userId, int limit) {
        Query query = new Query(Criteria.where("_id.userId").is(userId))
                .with(Sort.by(Sort.Direction.DESC, "playCount"))
                .limit(limit);
        return mongoTemplate.find(query, PlayCount.class).stream()
                .map(ListenService::toDto)
                .toList();
    }

    /**
     * @return counts for exactly the requested tracks, in the order asked for;
     * tracks never played report zero rather than being omitted
     */
    public List<TrackPlayCountDto> playCounts(String userId, List<String> trackIds) {
        Query query = new Query(Criteria.where("_id.userId").is(userId).and("_id.trackId").in(trackIds));
        Map<String, TrackPlayCountDto> found = mongoTemplate.find(query, PlayCount.class).stream()
                .map(ListenService::toDto)
                .collect(Collectors.toMap(TrackPlayCountDto::trackId, dto -> dto));

        return trackIds.stream()
                .map(trackId -> found.getOrDefault(trackId, new TrackPlayCountDto(trackId, 0, 0, null)))
                .toList();
    }

    /**
     * Clears the raw timeline but keeps the rollups, matching what the user asked
     * for: "forget what I listened to and when", not "reset my library".
     */
    public void clearTimeline(String userId) {
        mongoTemplate.remove(new Query(Criteria.where("userId").is(userId)), ListenEvent.class);
    }

    /** Full erasure across every listen-derived collection, for user deletion. */
    public void purge(String userId) {
        mongoTemplate.remove(new Query(Criteria.where("userId").is(userId)), ListenEvent.class);
        mongoTemplate.remove(new Query(Criteria.where("_id.userId").is(userId)), PlayCount.class);
        mongoTemplate.remove(new Query(Criteria.where("_id.userId").is(userId)), RecentlyPlayed.class);
    }

    private ListenEvent toDocument(String userId, ListenReportRequest report) {
        Integer trackMs = report.getTrackMs();
        boolean completed = trackMs != null && trackMs > 0
                && report.getMsPlayed() >= trackMs * COMPLETION_THRESHOLD;

        return ListenEvent.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .trackId(report.getTrackId())
                .albumId(report.getAlbumId())
                .playedAt(report.getPlayedAt())
                .msPlayed(report.getMsPlayed())
                .trackMs(trackMs)
                .completed(completed)
                .sourceType(report.getSourceType())
                .sourceId(report.getSourceId())
                .clientPlayId(report.getClientPlayId())
                .build();
    }

    private static TrackPlayCountDto toDto(PlayCount playCount) {
        return new TrackPlayCountDto(playCount.getId().getTrackId(), playCount.getPlayCount(),
                playCount.getMsPlayed(), playCount.getLastPlayedAt());
    }
}
