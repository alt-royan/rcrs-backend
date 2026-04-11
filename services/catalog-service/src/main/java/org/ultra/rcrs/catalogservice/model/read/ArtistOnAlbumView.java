package org.ultra.rcrs.catalogservice.model.read;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artist_on_album_view")
public class ArtistOnAlbumView {

    private UUID id;

    private String name;

    @Column("avatar_s3_key")
    @JsonProperty("avatar_s3_key")
    private String avatarS3Key;

    private ArtistRole role;
}