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
@Table(name = "audio_upload")
@NoArgsConstructor
@AllArgsConstructor
public class AudioUpload {

    @Id
    private String uid;

    @Enumerated(EnumType.STRING)
    private FileStatus status;

    @Column(name = "original_file_name")
    private String originalFileName;

    @Column(name = "track_id")
    private String trackId;

    @Column(name = "content_length")
    private Long contentLength;

    @Column(name = "expires_at")
    private Instant expiresAt;

    private String error;

}
