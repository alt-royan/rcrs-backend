package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.FileStatus;

import java.time.Instant;

@Data
@Builder
@Entity
@Table(name = "audio")
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    @Id
    private String uid;

    @Column(name = "track_id")
    private String trackId;

    @Column(name = "main")
    private Boolean isMain;

    @Column(name = "content_length")
    private Long contentLength;

    @Column(name = "duration_ms")
    private Long durationMs;

}
