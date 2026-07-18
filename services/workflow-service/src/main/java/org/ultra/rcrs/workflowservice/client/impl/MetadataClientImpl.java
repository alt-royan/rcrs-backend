package org.ultra.rcrs.workflowservice.client.impl;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.ultra.rcrs.workflowservice.client.MetadataClient;
import org.ultra.rcrs.workflowservice.config.temporal.ServiceProperties;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@Component
public class MetadataClientImpl implements MetadataClient {

    private final RestClient restClient;

    public MetadataClientImpl(RestClient.Builder builder, ServiceProperties properties) {
        this.restClient = builder
                .baseUrl(properties.metadata().url())
                .build();
    }

    @Override
    public ArtistResponse createArtist(CreateArtistRequest request) {
        return restClient.post()
                .uri("/api/artists")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ArtistResponse.class);
    }

    @Override
    public ArtistResponse updateArtist(String id, CreateArtistRequest request) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public ArtistResponse deleteArtist(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
