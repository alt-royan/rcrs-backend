package org.ultra.rcrs.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A single social media/external link associated with an artist.")
public record SocialLinkDto(
        @Schema(description = "Name of the social platform or resource (e.g. Instagram, Twitter, official website).")
        String resourceName,
        @Schema(description = "URL of the artist's profile/page on that platform.")
        String url
) {
}
