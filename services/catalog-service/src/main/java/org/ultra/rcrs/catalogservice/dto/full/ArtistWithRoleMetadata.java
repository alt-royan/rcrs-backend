package org.ultra.rcrs.catalogservice.dto.full;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.ArtistMetadataAbstract;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.utils.Url62;

@EqualsAndHashCode(callSuper = true)
@Data
public class ArtistWithRoleMetadata extends ArtistMetadataAbstract {

    private Boolean isArtist;

    private ArtistRole role;

    private String socialLink;

    public ArtistWithRoleMetadata(ArtistByEntity artist) {
        super(Url62.encode(artist.getArtistId()), artist.getArtistName(), S3Utils.createResourceS3Url(artist.getArtistAvatarKey()));
        this.isArtist = artist.getIsArtist();
        this.role = artist.getArtistRole();
        this.socialLink = artist.getSocialLink();
    }

}