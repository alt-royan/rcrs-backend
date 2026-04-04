package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.TrackMetadataAbstract;
import org.ultra.rcrs.catalogservice.model.track.Track;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleTrackMetadata extends TrackMetadataAbstract {

    private List<SimpleArtistMetadata> artists;

    public SimpleTrackMetadata(Track track, List<SimpleArtistMetadata> artists) {
        super(track);
        this.artists = artists;
    }

}