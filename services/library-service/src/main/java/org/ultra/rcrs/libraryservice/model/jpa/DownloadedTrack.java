package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.libraryservice.model.Quality;

import java.time.Instant;

/**
 * One track file present on the user's device.
 * <p>
 * Keyed without quality on purpose: there is one file per track on the device, so
 * re-downloading at a different quality replaces this row rather than creating a
 * second one.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "downloaded_tracks")
@IdClass(DownloadedTrackPK.class)
public class DownloadedTrack {

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Column(name = "track_id", nullable = false)
    private String trackId;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality", nullable = false)
    private Quality quality;

    /**
     * True when the user downloaded this single track directly rather than
     * receiving it as part of an album or playlist. Explicit tracks are kept even
     * after every collection containing them is removed.
     */
    @Column(name = "explicit", nullable = false)
    private boolean explicit;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
