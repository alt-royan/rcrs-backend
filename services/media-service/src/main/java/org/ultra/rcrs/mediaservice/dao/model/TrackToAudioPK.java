package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackToAudioPK implements Serializable {

    @Column(name = "track_id")
    private String trackId;

    private UUID guid;
}
