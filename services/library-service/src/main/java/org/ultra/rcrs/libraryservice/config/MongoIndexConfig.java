package org.ultra.rcrs.libraryservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.PartialIndexFilter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.libraryservice.model.mongo.ListenEvent;
import org.ultra.rcrs.libraryservice.model.mongo.PlayCount;
import org.ultra.rcrs.libraryservice.model.mongo.RecentlyPlayed;

import java.time.Duration;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Declares the Mongo indexes explicitly rather than relying on
 * {@code auto-index-creation}, because two of them cannot be expressed with
 * annotations: the TTL index that implements listen-history retention, and the
 * partial unique index that makes batch listen ingest idempotent.
 * <p>
 * Index creation is idempotent, so this runs safely on every start. Note that
 * MongoDB will reject a changed {@code expireAfterSeconds} on an existing index —
 * altering the retention window means dropping and recreating that index by hand.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MongoIndexConfig implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    /**
     * How long raw listen events are kept. Defaults to 13 months rather than 12 so
     * year-over-year features still have a full comparison window.
     */
    @Value("${library.listens.retention:400d}")
    private Duration listenRetention;

    @Override
    public void run(ApplicationArguments args) {
        var listens = mongoTemplate.indexOps(ListenEvent.class);

        // The timeline read: newest plays for one user.
        listens.createIndex(new Index().on("userId", ASC).on("playedAt", DESC).named("ix_listen_user_played"));

        // Retention. Mongo's background reaper sweeps roughly once a minute.
        listens.createIndex(new Index().on("playedAt", ASC)
                .expire(listenRetention)
                .named("ix_listen_ttl"));

        // Makes a retried ingest batch a no-op. Partial, because clientPlayId is
        // optional and many nulls would otherwise collide on a plain unique index.
        listens.createIndex(new Index().on("userId", ASC).on("clientPlayId", ASC)
                .unique()
                .partial(PartialIndexFilter.of(Criteria.where("clientPlayId").exists(true)))
                .named("ux_listen_client_play_id"));

        var playCounts = mongoTemplate.indexOps(PlayCount.class);
        playCounts.createIndex(new Index().on("_id.userId", ASC).on("playCount", DESC).named("ix_play_counts_top"));
        playCounts.createIndex(new Index().on("_id.userId", ASC).on("lastPlayedAt", DESC).named("ix_play_counts_recent"));

        var recentlyPlayed = mongoTemplate.indexOps(RecentlyPlayed.class);
        recentlyPlayed.createIndex(new Index().on("_id.userId", ASC).on("lastPlayedAt", DESC).named("ix_recently_played"));

        log.info("Mongo indexes ensured; listen history retention is {} days", listenRetention.toDays());
    }

    /**
     * Exposed so tests can assert the TTL index really exists with the configured
     * expiry — the one property of this configuration that silently does nothing
     * if it is wrong.
     */
    public Document listenTtlIndex() {
        for (Document index : mongoTemplate.getCollection(
                mongoTemplate.getCollectionName(ListenEvent.class)).listIndexes()) {
            if ("ix_listen_ttl".equals(index.getString("name"))) {
                return index;
            }
        }
        return null;
    }
}
