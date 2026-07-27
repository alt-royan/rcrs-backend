package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.mediaservice.enums.Quality;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID guid;

    private String key;

    private String codec;

    private String container;

    private String contentType;

    @Column(name = "duration_ms")
    private Integer durationMs;

    private String bitrate;

    @Enumerated(EnumType.STRING)
    private Quality quality;

    @Column(name = "sample_rate")
    private String sampleRate;

    @Column(name = "byte_size")
    private Long byteSize;

    @Column(name = "creation_timestamp")
    private Instant creationTimestamp;
}
