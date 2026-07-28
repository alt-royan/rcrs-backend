package org.ultra.rcrs.libraryservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.ultra.rcrs.libraryservice.model.PlaybackSource;

import java.time.Instant;

/**
 * One playback, appended and never updated.
 * <p>
 * This is the raw log. Anything the UI reads repeatedly — the recently-played shelf,
 * play counts — is served from {@link RecentlyPlayed} and {@link PlayCount} instead,
 * so this collection is only scanned for the full history timeline.
 * <p>
 * A TTL index on {@code playedAt} expires documents here; the rollups are not
 * expired, so ageing raw history out loses nothing the user can see.
 * Indexes are declared in {@code MongoIndexConfig}, not by annotation, because the
 * TTL and the partial unique index cannot be expressed cleanly here.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "listen_events")
public class ListenEvent {

    @Id
    private String id;

    private String userId;

    private String trackId;

    /** Denormalized so "recently played albums" needs no lookup. */
    private String albumId;

    private Instant playedAt;

    private int msPlayed;

    private Integer trackMs;

    /** Computed once at write time; never recomputed on read. */
    private boolean completed;

    /** Where playback was started from, when the client reports it. */
    private PlaybackSource sourceType;

    private String sourceId;

    /** Client-generated key that makes a retried batch idempotent. */
    private String clientPlayId;
}
