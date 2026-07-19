package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.ArtistActivity;
import org.ultra.rcrs.workflow.client.ArtistClient;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.RegisterArtistRequest;

@Component
@ActivityImpl
public class ArtistActivityImpl implements ArtistActivity {

    private final ArtistClient artistClient;

    public ArtistActivityImpl(ArtistClient artistClient) {
        this.artistClient = artistClient;
    }

    @Override
    public ResponseEntity<CreateResponse> createArtist(RegisterArtistRequest request) {
        return artistClient.createArtist(request);
    }

    @Override
    public ResponseEntity<Void> markArtistDeleted(String id) {
        return artistClient.markArtistDeleted(id);
    }

    @Override
    public ResponseEntity<Void> hideArtist(String id) {
        return artistClient.hideArtist(id);
    }

    @Override
    public ResponseEntity<Void> activeArtist(String id) {
        return artistClient.activeArtist(id);
    }
}
