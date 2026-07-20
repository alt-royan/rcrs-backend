package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.workflow.client.model.AlbumUploadModel;
import org.ultra.rcrs.workflow.client.model.ArtistsToEntityModel;
import org.ultra.rcrs.workflow.dto.response.CreateResponse;
import org.ultra.rcrs.workflow.dto.StatusDto;

@ActivityInterface
public interface AlbumActivity {

    @ActivityMethod
    CreateResponse createAlbum(AlbumUploadModel request);

    @ActivityMethod
    void addArtistsToAlbum(ArtistsToEntityModel request, String albumId);

    @ActivityMethod
    void deleteArtistsFromAlbum(ArtistsToEntityModel request, String albumId);

    @ActivityMethod
    void updateAlbumStatus(StatusDto statusDto, String albumId);

    @ActivityMethod
    void hideAlbum(String albumId);

    @ActivityMethod
    void activeAlbum(String albumId);

    @ActivityMethod
    void markAlbumDeleted(String albumId);
}
