package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.dto.ImageDto;
import org.ultra.rcrs.catalogservice.model.ArtistById;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Base62Utils;

@Data
public class ArtistSimplifyDto {

    private String id;

    private String name;

    private ImageDto avatarImage;

    public ArtistSimplifyDto(ArtistById artist) {
        this.id = Base62Utils.encode(artist.getArtistId());
        this.name = artist.getName();
        this.avatarImage = new ImageDto(S3Utils.createResourceS3Url(artist.getImageKey()));
    }

}