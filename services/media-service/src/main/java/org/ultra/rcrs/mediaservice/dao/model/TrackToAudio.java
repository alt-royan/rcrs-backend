package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "track_to_audio")
@NoArgsConstructor
@AllArgsConstructor
@IdClass(TrackToAudioPK.class)
public class TrackToAudio {

    @Id
    @Column(name = "track_id")
    private String trackId;

    @Id
    private UUID guid;

    private Boolean main;
}
