package org.ultra.rcrs.catalogservice.dto.full;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.ultra.rcrs.catalogservice.dto.AlbumMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleTrackMetadata;
import org.ultra.rcrs.catalogservice.model.album.Album;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullAlbumMetadata extends AlbumMetadataAbstract {

    private List<SimpleTrackMetadata> tracks;

    private List<ArtistWithRoleMetadata> artists;

    public FullAlbumMetadata(Album album, List<ArtistWithRoleMetadata> artists, List<SimpleTrackMetadata> tracks) {
        super(album);
        this.artists = artists;
        this.tracks = tracks;
    }

}