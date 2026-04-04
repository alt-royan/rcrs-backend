package org.ultra.rcrs.catalogservice.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ArtistMetadataAbstract {

    private String id;

    private String name;

    private String avatarUrl;

    protected ArtistMetadataAbstract(Artist artist) {
        this.id = Url62.encode(artist.getArtistId());
        this.name = artist.getName();
        this.avatarUrl = S3Utils.createResourceS3Url(artist.getAvatarKey());
    }

}