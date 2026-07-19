package org.ultra.rcrs.workflow.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import org.springframework.http.ResponseEntity;
import org.ultra.rcrs.workflow.dto.AlbumUploadRequest;
import org.ultra.rcrs.workflow.dto.ArtistsToEntityRequest;
import org.ultra.rcrs.workflow.dto.CreateResponse;
import org.ultra.rcrs.workflow.dto.StatusDto;

@ActivityInterface
public interface AlbumActivity {

    @ActivityMethod
    ResponseEntity<CreateResponse> createAlbum(AlbumUploadRequest request);

    @ActivityMethod
    ResponseEntity<Void> addArtistsToAlbum(ArtistsToEntityRequest request, String albumId);

    @ActivityMethod
    ResponseEntity<Void> deleteArtistsFromAlbum(ArtistsToEntityRequest request, String albumId);

    @ActivityMethod
    ResponseEntity<Void> updateAlbumStatus(StatusDto statusDto, String albumId);

    @ActivityMethod
    ResponseEntity<Void> hideAlbum(String albumId);

    @ActivityMethod
    ResponseEntity<Void> activeAlbum(String albumId);

    @ActivityMethod
    ResponseEntity<Void> markAlbumDeleted(String albumId);
}
