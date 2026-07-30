package org.ultra.rcrs.libraryservice.model.jpa;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadedTrackPK implements Serializable {

    @Column(name = "user_id")
    private String userId;

    @Column(name = "track_id")
    private String trackId;
}
