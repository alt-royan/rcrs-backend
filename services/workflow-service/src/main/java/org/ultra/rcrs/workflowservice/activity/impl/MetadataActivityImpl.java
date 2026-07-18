package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.MetadataActivity;
import org.ultra.rcrs.workflowservice.client.MetadataClient;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@Component
public class MetadataActivityImpl implements MetadataActivity {

    private final MetadataClient metadataClient;

    public MetadataActivityImpl(MetadataClient metadataClient) {
        this.metadataClient = metadataClient;
    }

    @Override
    public ArtistResponse createArtist(CreateArtistRequest request) {
        return metadataClient.createArtist(request);
    }

    @Override
    public ArtistResponse updateArtist(String id, CreateArtistRequest request) {
        return metadataClient.updateArtist(id, request);
    }

    @Override
    public ArtistResponse deleteArtist(String id) {
        return metadataClient.deleteArtist(id);
    }
}
