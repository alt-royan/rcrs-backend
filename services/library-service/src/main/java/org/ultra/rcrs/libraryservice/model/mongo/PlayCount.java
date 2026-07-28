package org.ultra.rcrs.libraryservice.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * How often the caller has played one track, maintained by upsert as listens are
 * ingested so "your top tracks" never has to aggregate the raw log.
 * <p>
 * Not expired by any TTL: this is the durable record that survives raw history
 * ageing out.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "play_counts")
public class PlayCount {

    @Id
    private PlayCountId id;

    private long playCount;

    private long msPlayed;

    private Instant firstPlayedAt;

    private Instant lastPlayedAt;
}
