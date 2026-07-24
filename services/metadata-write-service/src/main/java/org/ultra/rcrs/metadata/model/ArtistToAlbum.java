package org.ultra.rcrs.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "artist_to_album")
@IdClass(ArtistToAlbumPK.class)
public class ArtistToAlbum {

    @Id
    @Column(name = "artist_id")
    private UUID artistId;

    @Id
    @Column(name = "album_id")
    private UUID albumId;

    @Column(name = "artist_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private ArtistRole artistRole;
}
