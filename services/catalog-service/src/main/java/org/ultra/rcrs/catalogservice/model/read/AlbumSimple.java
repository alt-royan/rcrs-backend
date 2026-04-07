package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AlbumSimple {

    @Id
    private UUID id;

    private String title;

    @Column("cover_s3_key")
    private String coverS3Key;

}
