package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.ArtistActivity;
import org.ultra.rcrs.workflow.client.ArtistClient;
import org.ultra.rcrs.workflow.client.model.ArtistCreateModel;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;

@Component
@ActivityImpl
public class ArtistActivityImpl implements ArtistActivity {

    private final ArtistClient artistClient;

    public ArtistActivityImpl(ArtistClient artistClient) {
        this.artistClient = artistClient;
    }

    @Override
    public CreateResponse createArtist(ArtistCreateModel request) {
        var res = artistClient.createArtist(request);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new RuntimeException("Unsupported behavior: response is not 2xx");
        }
        return res.getBody();
    }

    @Override
    public void markArtistDeleted(String id) {
        artistClient.markArtistDeleted(id);
    }

    @Override
    public void hideArtist(String id) {
        artistClient.hideArtist(id);
    }

    @Override
    public void activeArtist(String id) {
        artistClient.activeArtist(id);
    }
}
