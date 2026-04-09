package org.ultra.rcrs.catalogservice.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import org.ultra.rcrs.catalogservice.model.write.OthersOnTrack;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Set;

@Data
public class ArtistOtherDto {

    private String name;

    private SocialLinkDto socialLink;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Set<ArtistRole> roles;

    public ArtistOtherDto(OthersOnTrack.ArtistOther other) {
        this.name = other.getName();
        this.socialLink = new SocialLinkDto(other.getSocialLink());
        this.roles = other.getRoles();
    }
}