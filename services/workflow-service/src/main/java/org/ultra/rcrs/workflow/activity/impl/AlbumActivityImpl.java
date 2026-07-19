package org.ultra.rcrs.workflow.activity.impl;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.activity.AlbumActivity;
import org.ultra.rcrs.workflow.client.AlbumClient;
import org.ultra.rcrs.workflow.dto.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.ArtistsToEntityRequest;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.StatusDto;


@Component
@ActivityImpl
public class AlbumActivityImpl implements AlbumActivity {

    private final AlbumClient albumClient;

    public AlbumActivityImpl(AlbumClient albumClient) {
        this.albumClient = albumClient;
    }

    @Override
    public ResponseEntity<CreateResponse> createAlbum(AlbumUploadRequest request) {
        return albumClient.createAlbum(request);
    }

    @Override
    public ResponseEntity<Void> addArtistsToAlbum(ArtistsToEntityRequest request, String albumId) {
        return albumClient.addArtistsToAlbum(request, albumId);
    }

    @Override
    public ResponseEntity<Void> deleteArtistsFromAlbum(ArtistsToEntityRequest request, String albumId) {
        return albumClient.deleteArtistsFromAlbum(request, albumId);
    }

    @Override
    public ResponseEntity<Void> updateAlbumStatus(StatusDto statusDto, String albumId) {
        return albumClient.updateAlbumStatus(statusDto, albumId);
    }

    @Override
    public ResponseEntity<Void> hideAlbum(String albumId) {
        return albumClient.hideAlbum(albumId);
    }

    @Override
    public ResponseEntity<Void> activeAlbum(String albumId) {
        return albumClient.activeAlbum(albumId);
    }

    @Override
    public ResponseEntity<Void> markAlbumDeleted(String albumId) {
        return albumClient.markAlbumDeleted(albumId);
    }
}
