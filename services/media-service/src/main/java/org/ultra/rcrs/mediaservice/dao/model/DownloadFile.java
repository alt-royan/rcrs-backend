package org.ultra.rcrs.mediaservice.dao.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "download_file")
@NoArgsConstructor
@AllArgsConstructor
public class DownloadFile {

    /**
     * Same id as the {@link Audio} rendition this downloadable file was made of.
     */
    @Id
    private UUID id;

    private UUID guid;

    private String key;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "creation_timestamp")
    private OffsetDateTime creationTimestamp;
}
