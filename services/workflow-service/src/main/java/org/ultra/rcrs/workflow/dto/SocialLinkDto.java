package org.ultra.rcrs.workflow.dto;

import jakarta.validation.constraints.NotBlank;

import java.net.URI;


public record SocialLinkDto(
        @NotBlank String resourceName,
        @NotBlank URI url
) {
}
