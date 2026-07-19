package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.MetadataActivity;
import org.ultra.rcrs.workflow.client.MetadataWriteClient;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@Component
@ActivityImpl
public class MetadataActivityImpl implements MetadataActivity {

    private final MetadataWriteClient metadataWriteClient;

    public MetadataActivityImpl(MetadataWriteClient metadataWriteClient) {
        this.metadataWriteClient = metadataWriteClient;
    }

    @Override
    public CreateResponse createArtist(RegisterArtistRequest request) {
        return metadataWriteClient.createArtist(request);
    }
}
