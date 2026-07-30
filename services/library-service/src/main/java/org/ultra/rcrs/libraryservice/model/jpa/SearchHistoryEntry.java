package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

import java.time.Instant;

/**
 * One entity the user opened from search results. The primary key is the entity
 * rather than a surrogate id, so re-opening the same entity updates the existing
 * row instead of accumulating duplicates.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "search_history")
@IdClass(SearchHistoryPK.class)
public class SearchHistoryEntry {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private LibraryEntityType entityType;

    @Id
    @Column(name = "entity_id", nullable = false)
    private String entityId;

    /** The query that led to this entity, when the client knows it. */
    @Column(name = "query_raw")
    private String queryRaw;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;
}
