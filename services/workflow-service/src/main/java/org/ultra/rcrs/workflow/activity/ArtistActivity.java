package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@ActivityInterface
public interface ArtistActivity {

    @ActivityMethod
    ResponseEntity<CreateResponse> createArtist(RegisterArtistRequest request);

    @ActivityMethod
    ResponseEntity<Void> markArtistDeleted(String id);

    @ActivityMethod
    ResponseEntity<Void> hideArtist(String id);

    @ActivityMethod
    ResponseEntity<Void> activeArtist(String id);
}
