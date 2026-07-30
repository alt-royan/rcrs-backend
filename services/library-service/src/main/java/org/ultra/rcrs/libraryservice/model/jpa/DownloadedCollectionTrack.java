package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.libraryservice.model.LibraryEntityType;

/**
 * Which tracks arrived with which downloaded collection.
 * <p>
 * Without this, removing one downloaded album could not tell whether a track is
 * still needed by another downloaded collection, and would either strand files on
 * the device or delete tracks the user still has elsewhere.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "downloaded_collection_tracks")
@IdClass(DownloadedCollectionTrackPK.class)
public class DownloadedCollectionTrack {

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

    @Id
    @Column(name = "track_id", nullable = false)
    private String trackId;
}
