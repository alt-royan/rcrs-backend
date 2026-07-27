package org.ultra.rcrs.playlistservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(nullable = false)
    private String title;

    private String description;

    private List<String> tags;

    @Column(name = "cover_s3_key")
    private String coverS3Key;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PlaylistType type;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
