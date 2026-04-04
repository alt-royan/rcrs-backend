package org.ultra.rcrs.catalogservice.dto.simplify;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.AlbumMetadataAbstract;
import org.ultra.rcrs.catalogservice.model.album.Album;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SimpleAlbumMetadata extends AlbumMetadataAbstract {

    private List<SimpleArtistMetadata> artists;

    public SimpleAlbumMetadata(Album album, List<SimpleArtistMetadata> artists) {
        super(album);
        this.artists = artists;
    }

}