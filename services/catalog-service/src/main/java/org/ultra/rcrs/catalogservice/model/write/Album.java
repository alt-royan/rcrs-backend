package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("albums")
public class Album {

    private UUID id;

    @Column("status")
    private EntityStatus status;

    @Column("title")
    private String title;

    @Column("type")
    private AlbumType type;

    @Column("release_date")
    private Instant releaseDate;

    @Column("cover_s3_key")
    private String coverS3Key;

    @Column("available")
    private Boolean available;
}
