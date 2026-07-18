package org.ultra.rcrs.workflowservice.activity;

import org.ultra.rcrs.workflowservice.dto.ArtistResponse;
import org.ultra.rcrs.workflowservice.dto.CreateArtistRequest;

public interface MetadataActivity {

    ArtistResponse createArtist(CreateArtistRequest request);

    ArtistResponse updateArtist(String id, CreateArtistRequest request);

    ArtistResponse deleteArtist(String id);
}
