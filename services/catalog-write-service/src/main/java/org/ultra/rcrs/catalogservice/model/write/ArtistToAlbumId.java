package org.ultra.rcrs.catalogservice.model.write;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistToAlbumId implements Serializable {

    @Column(name = "artist_id")
    private UUID artistId;

    @Column(name = "album_id")
    private UUID albumId;
}
