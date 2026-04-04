package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.TrackMetadataAbstract;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleTrackWithAlbumIdMetadata extends TrackMetadataAbstract {

    private List<SimpleArtistMetadata> artists;

    private String albumId;

    public SimpleTrackWithAlbumIdMetadata(Track track, UUID albumId, List<SimpleArtistMetadata> artists) {
        super(track);
        this.artists = artists;
        this.albumId = Url62.encode(albumId);
    }

}