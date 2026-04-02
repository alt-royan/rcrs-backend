package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

@Data
public class ArtistSimplifyDto {

    private String id;

    private String name;

    public ArtistSimplifyDto(Artist artist) {
        this.id = Url62.encode(artist.getArtistId());
        this.name = artist.getName();
    }

}