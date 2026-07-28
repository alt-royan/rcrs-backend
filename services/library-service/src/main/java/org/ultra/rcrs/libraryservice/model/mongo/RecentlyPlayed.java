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
 * The home-screen "recently played" shelf: one document per entity the caller has
 * played, overwritten on every play.
 * <p>
 * Derived from {@link ListenEvent} rather than queried from it, because building
 * the shelf from the log would mean a distinct-and-sort over an ever-growing
 * collection on every screen load. Trimmed to a fixed number of entries per user.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "recently_played")
public class RecentlyPlayed {

    @Id
    private RecentlyPlayedId id;

    private Instant lastPlayedAt;

    private long playCount;
}
