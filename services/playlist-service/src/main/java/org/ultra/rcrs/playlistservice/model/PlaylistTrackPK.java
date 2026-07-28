package org.ultra.rcrs.playlistservice.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaylistTrackPK implements Serializable {

    @Column(name = "playlist_id")
    private UUID playlistId;

    @Column(name = "track_id")
    private String trackId;
}
