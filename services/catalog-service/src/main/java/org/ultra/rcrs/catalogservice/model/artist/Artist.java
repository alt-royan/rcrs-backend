package org.ultra.rcrs.catalogservice.model.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterDto;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artists_by_id")
public class Artist {

    @PrimaryKey
    @Column("artist_id")
    private UUID artistId;

    @Column("name")
    private String name;

    @Column("bio")
    private String bio;

    @Column("image_key")
    private String imageKey;

    public Artist(ArtistRegisterDto dto) {
        this.name = dto.getName();
        this.bio = dto.getBio();
        this.imageKey = dto.getImageExternalKey();
    }
}