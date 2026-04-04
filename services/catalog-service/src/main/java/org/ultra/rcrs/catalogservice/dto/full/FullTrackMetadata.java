package org.ultra.rcrs.catalogservice.dto.full;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.TrackMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleAlbumMetadata;
import org.ultra.rcrs.catalogservice.model.track.Track;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullTrackMetadata extends TrackMetadataAbstract {

    private SimpleAlbumMetadata album;

    private List<ArtistWithRoleMetadata> artists;

    public FullTrackMetadata(Track track, SimpleAlbumMetadata album, List<ArtistWithRoleMetadata> artists) {
        super(track);
        this.album = album;
        this.artists = artists;
    }

}