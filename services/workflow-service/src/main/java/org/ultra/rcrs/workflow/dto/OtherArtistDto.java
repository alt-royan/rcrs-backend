package org.ultra.rcrs.workflow.dto;

import org.ultra.rcrs.enums.ArtistRole;

import java.util.List;
import java.util.Set;

public record OtherArtistDto(
        String id,
        String name,
        Set<ArtistRole> roles,
        List<SocialLinkDto> socialLinks
) {
}
