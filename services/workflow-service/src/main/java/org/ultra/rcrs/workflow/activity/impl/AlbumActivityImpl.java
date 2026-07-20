package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.AlbumActivity;
import org.ultra.rcrs.workflow.client.AlbumClient;
import org.ultra.rcrs.workflow.client.model.AlbumUploadModel;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.dto.StatusDto;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;


@Component
@ActivityImpl
public class AlbumActivityImpl implements AlbumActivity {

    private final AlbumClient albumClient;

    public AlbumActivityImpl(AlbumClient albumClient) {
        this.albumClient = albumClient;
    }

    @Override
    public CreateResponse createAlbum(AlbumUploadModel request) {
        var res = albumClient.createAlbum(request);
        if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) {
            throw new RuntimeException("Unsupported behavior: response is not 2xx");
        }
        return res.getBody();
    }

    @Override
    public void addArtistsToAlbum(ArtistsToEntityModel request, String albumId) {
        albumClient.addArtistsToAlbum(request, albumId);
    }

    @Override
    public void deleteArtistsFromAlbum(ArtistsToEntityModel request, String albumId) {
        albumClient.deleteArtistsFromAlbum(request, albumId);
    }

    @Override
    public void updateAlbumStatus(StatusDto statusDto, String albumId) {
        albumClient.updateAlbumStatus(statusDto, albumId);
    }

    @Override
    public void hideAlbum(String albumId) {
        albumClient.hideAlbum(albumId);
    }

    @Override
    public void activeAlbum(String albumId) {
        albumClient.activeAlbum(albumId);
    }

    @Override
    public void markAlbumDeleted(String albumId) {
        albumClient.markAlbumDeleted(albumId);
    }
}
