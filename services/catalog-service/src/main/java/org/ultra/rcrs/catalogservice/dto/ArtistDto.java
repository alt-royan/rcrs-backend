package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.ArtistById;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Base62Utils;

@Data
public class ArtistDto {

    private String id;

    private String name;

    private String bio;

    private ImageDto avatarImage;

    public ArtistDto(ArtistById artist) {
        this.id = Base62Utils.encode(artist.getArtistId());
        this.name = artist.getName();
        this.bio = artist.getBio();
        this.avatarImage = new ImageDto(S3Utils.createResourceS3Url(artist.getImageKey()));
    }

}