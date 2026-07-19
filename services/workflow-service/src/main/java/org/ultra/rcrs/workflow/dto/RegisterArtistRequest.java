package org.ultra.rcrs.workflow.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RegisterArtistRequest(
        @NotBlank
        String name,
        String avatarUri,
        List<SocialLinkDto>socialLinks,
        List<String> tags
) {}
