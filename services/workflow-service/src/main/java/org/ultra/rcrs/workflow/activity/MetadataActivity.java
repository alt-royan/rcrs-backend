package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@ActivityInterface
public interface MetadataActivity {

    @ActivityMethod
    CreateResponse createArtist(RegisterArtistRequest request);
}
