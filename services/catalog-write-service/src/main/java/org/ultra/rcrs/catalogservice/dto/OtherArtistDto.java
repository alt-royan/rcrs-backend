package org.ultra.rcrs.catalogservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public class OtherArtistDto {

    private String id;

    private String name;

    private Set<ArtistRole> roles;

    private List<SocialLinkDto> socialLinks = new ArrayList<>();
}
