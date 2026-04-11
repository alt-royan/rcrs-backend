package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artist_view")
public class ArtistView {

    private UUID id;

    @Column("name")
    private String name;

    @Column("avatar_s3_key")
    private String avatarS3Key;

}