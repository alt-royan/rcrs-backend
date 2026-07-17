package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.FileStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "audio")
@NoArgsConstructor
@AllArgsConstructor
public class Audio {

    @Id
    private UUID guid;

    @Column(name = "upload_uid")
    private String upload_uid;

    @Column(name = "track_id")
    private String trackId;

    private String codec;

    private String container;

    private String bitrate;
}
