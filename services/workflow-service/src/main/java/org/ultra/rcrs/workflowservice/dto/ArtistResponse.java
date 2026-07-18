package org.ultra.rcrs.workflowservice.dto;

public record ArtistResponse(
    String id,
    String name,
    String bio,
    String imageUrl
) {}
