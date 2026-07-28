package org.ultra.rcrs.metadata.repository;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.TrackDocument;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;

/**
 * Album track counts and durations are not stored on the album document — they are
 * derived from the tracks collection on read.
 */
@Repository
@RequiredArgsConstructor
public class AlbumTotalsRepository {

    public record AlbumTotals(Integer totalTracks, Integer totalDurationMs) {

        public static final AlbumTotals EMPTY = new AlbumTotals(0, 0);
    }

    private final ReactiveMongoTemplate mongoTemplate;

    /**
     * Totals over every track of the album, whatever its status.
     */
    public Mono<Map<String, AlbumTotals>> findTotalsForAdmin(Collection<String> albumIds) {
        return findTotals(albumIds, Criteria.where("album.id").in(albumIds));
    }

    /**
     * Totals over the tracks a storefront visitor can see — same filter as
     * {@link org.ultra.rcrs.metadata.service.publ.TrackPublicService#getAllByAlbumId}.
     */
    public Mono<Map<String, AlbumTotals>> findTotalsForPublic(Collection<String> albumIds) {
        return findTotals(albumIds, Criteria.where("album.id").in(albumIds)
                .and("lifecycleStatus").is("PUBLISHED")
                .and("availabilityStatus").in("ACTIVE", "HIDDEN"));
    }

    public static AlbumTotals totalsOf(Map<String, AlbumTotals> totals, String albumId) {
        return totals.getOrDefault(albumId, AlbumTotals.EMPTY);
    }

    private Mono<Map<String, AlbumTotals>> findTotals(Collection<String> albumIds, Criteria criteria) {
        if (albumIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        // typed aggregation so "album.id" is mapped to the stored field name ("album._id")
        Aggregation aggregation = Aggregation.newAggregation(TrackDocument.class,
                Aggregation.match(criteria),
                Aggregation.group("album.id")
                        .count().as("totalTracks")
                        .sum(ConditionalOperators.ifNull("durationMs").then(0)).as("totalDurationMs"));

        return mongoTemplate.aggregate(aggregation, "tracks", Document.class)
                .collectMap(doc -> doc.getString("_id"), AlbumTotalsRepository::toTotals);
    }

    private static AlbumTotals toTotals(Document doc) {
        Number tracks = doc.get("totalTracks", Number.class);
        Number duration = doc.get("totalDurationMs", Number.class);
        return new AlbumTotals(
                tracks != null ? tracks.intValue() : 0,
                duration != null ? duration.intValue() : 0);
    }
}
