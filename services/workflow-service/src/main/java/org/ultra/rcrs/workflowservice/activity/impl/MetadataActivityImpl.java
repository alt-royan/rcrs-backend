package org.ultra.rcrs.workflowservice.activity.impl;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflowservice.activity.MetadataActivity;
import org.ultra.rcrs.workflowservice.client.MetadataWriteClient;
import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

@Component
public class MetadataActivityImpl implements MetadataActivity {

    private final MetadataWriteClient metadataWriteClient;

    public MetadataActivityImpl(MetadataWriteClient metadataWriteClient) {
        this.metadataWriteClient = metadataWriteClient;
    }

    @Override
    public ArtistResponse createArtist(CreateArtistRequest request) {
        return metadataWriteClient.createArtist(request);
    }

    @Override
    public ArtistResponse updateArtist(String id, CreateArtistRequest request) {
        return metadataWriteClient.updateArtist(id, request);
    }

    @Override
    public ArtistResponse deleteArtist(String id) {
        return metadataWriteClient.deleteArtist(id);
    }
}
