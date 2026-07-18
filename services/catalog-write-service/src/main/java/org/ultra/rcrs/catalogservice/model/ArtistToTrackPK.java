package org.ultra.rcrs.catalogservice.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArtistToTrackPK implements Serializable {

    @Column(name = "artist_id")
    private UUID artistId;

    @Column(name = "track_id")
    private UUID trackId;
}
