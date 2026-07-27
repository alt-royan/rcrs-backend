package org.ultra.rcrs.playlistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlist_tracks")
@IdClass(PlaylistTrackPK.class)
public class PlaylistTrack {

    @Id
    @Column(name = "playlist_id", nullable = false)
    private UUID playlistId;

    @Id
    @Column(name = "track_id", nullable = false)
    private String trackId;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
