package org.ultra.rcrs.workflow.converter;

import org.springframework.stereotype.Component;
import org.ultra.rcrs.workflow.client.model.AlbumUploadModel;
import org.ultra.rcrs.workflow.client.model.ArtistCreateModel;
import org.ultra.rcrs.workflow.client.model.TrackUploadModel;
import org.ultra.rcrs.workflow.dto.request.ArtistUploadRequest;
import org.ultra.rcrs.workflow.dto.request.TrackUploadRequest;

@Component
public final class UploadRequestConverter {

    public ArtistCreateModel toArtistCreateModel(ArtistUploadRequest request) {
        return new ArtistCreateModel(
                request.name(),
                request.avatarUri(),
                request.socialLinks(),
                request.tags()
        );
    }

    public AlbumUploadModel toAlbumCreateModel(org.ultra.rcrs.workflow.dto.request.AlbumUploadRequest request) {
        return new AlbumUploadModel(
                request.title(),
                request.type(),
                request.releaseDate(),
                request.publishTimestamp(),
                request.coverUri()
        );
    }

    public TrackUploadModel toTrackCreateModel(TrackUploadRequest request, String albumId) {
        return new TrackUploadModel(
                albumId,
                request.title(),
                request.trackNumber(),
                request.explicit()
        );
    }
}
