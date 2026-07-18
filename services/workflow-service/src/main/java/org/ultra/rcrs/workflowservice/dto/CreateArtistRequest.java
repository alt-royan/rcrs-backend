package org.ultra.rcrs.workflowservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateArtistRequest(
    @NotBlank String name,
    String bio,
    String imageUrl
) {}
