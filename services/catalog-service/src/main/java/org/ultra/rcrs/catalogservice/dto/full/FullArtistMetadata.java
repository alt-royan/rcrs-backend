package org.ultra.rcrs.catalogservice.dto.full;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.ArtistMetadataAbstract;
import org.ultra.rcrs.catalogservice.model.artist.Artist;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullArtistMetadata extends ArtistMetadataAbstract {

    private String socialLink;

    public FullArtistMetadata(Artist artist) {
        super(artist);
        this.socialLink = artist.getSocialLink();
    }

}