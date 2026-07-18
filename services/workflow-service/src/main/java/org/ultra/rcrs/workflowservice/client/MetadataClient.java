package org.ultra.rcrs.workflowservice.client;

import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

public interface MetadataClient {

    ArtistResponse createArtist(CreateArtistRequest request);

    ArtistResponse updateArtist(String id, CreateArtistRequest request);

    ArtistResponse deleteArtist(String id);
}
