package org.ultra.rcrs.workflow.client.model;

import org.ultra.rcrs.workflow.dto.SocialLinkDto;

import java.util.List;

public record ArtistCreateModel(
        String name,
        String avatarUri,
        List<SocialLinkDto> socialLinks,
        List<String> tags
) {
}
