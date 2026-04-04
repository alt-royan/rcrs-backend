package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

@Data
public class SimpleArtistMetadata {

    private String id;

    private String name;

    private String avatarUrl;

    public SimpleArtistMetadata(ArtistByEntity artist) {
        this.id = Url62.encode(artist.getArtistId());
        this.name = artist.getArtistName();
        this.avatarUrl = S3Utils.createResourceS3Url(artist.getArtistAvatarKey());
    }

}