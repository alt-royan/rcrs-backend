package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.Artist;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

@Data
public class ArtistDto {

    private String id;

    private String name;

    private String bio;

    private ImageDto avatarImage;

    public ArtistDto(Artist artist) {
        this.id = Url62.encode(artist.getArtistId());
        this.name = artist.getName();
        this.bio = artist.getBio();
        this.avatarImage = new ImageDto(S3Utils.createResourceS3Url(artist.getImageKey()));
    }

}