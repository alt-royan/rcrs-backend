package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;
import org.ultra.rcrs.libraryservice.model.Quality;

import java.time.Instant;

/**
 * What the user believes they downloaded — an album or a playlist — so the
 * Downloads screen can show one card instead of a dozen loose tracks. The files
 * themselves are tracked by {@link DownloadedTrack}.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "downloaded_collections")
@IdClass(DownloadedCollectionPK.class)
public class DownloadedCollection {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "quality", nullable = false)
    private Quality quality;

    @Column(name = "track_count", nullable = false)
    private int trackCount;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
