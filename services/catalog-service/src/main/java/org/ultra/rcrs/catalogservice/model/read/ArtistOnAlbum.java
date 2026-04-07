package org.ultra.rcrs.catalogservice.model.read;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ArtistOnAlbum {

    private UUID id;

    private String name;

    @Column("avatar_s3_key")
    private String avatarS3Key;

    private ArtistRole role;
}