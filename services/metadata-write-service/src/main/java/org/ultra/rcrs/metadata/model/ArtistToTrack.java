package org.ultra.rcrs.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artist_to_track")
@IdClass(ArtistToTrackPK.class)
public class ArtistToTrack {

    @Id
    @Column(name = "artist_id")
    private UUID artistId;

    @Id
    @Column(name = "track_id")
    private UUID trackId;

    @Column(name = "artist_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArtistRole artistRole;
}
