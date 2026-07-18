package org.ultra.rcrs.workflowservice.mapper;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@Component
public class ArtistMapper {

    public ArtistResponse toResponse(String id, CreateArtistRequest request) {
        return new ArtistResponse(id, request.name(), request.bio(), request.imageUrl());
    }
}
