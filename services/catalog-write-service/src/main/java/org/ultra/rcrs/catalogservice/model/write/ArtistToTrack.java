package org.ultra.rcrs.catalogservice.model.write;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artist_to_track")
@IdClass(ArtistToTrackId.class)
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
