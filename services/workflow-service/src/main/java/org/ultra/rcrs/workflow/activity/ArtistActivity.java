package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.ultra.rcrs.workflow.client.model.ArtistCreateModel;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@ActivityInterface
public interface ArtistActivity {

    @ActivityMethod
    CreateResponse createArtist(ArtistCreateModel request);

    @ActivityMethod
    void markArtistDeleted(String id);

    @ActivityMethod
    void hideArtist(String id);

    @ActivityMethod
    void activeArtist(String id);
}
