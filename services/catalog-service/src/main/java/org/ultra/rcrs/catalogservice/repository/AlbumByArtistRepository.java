package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.enums.AlbumStatus;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlbumByArtistRepository extends ReactiveCassandraRepository<AlbumByArtist, AlbumByArtist.AlbumByArtistKey> {

    @Query("SELECT * FROM albums_by_artist WHERE artistId = ? and album_status IN ? and artist_role IN ? and album_type IN ? ORDER BY release_date ?")
    Flux<AlbumByArtist> findAll(UUID artistId, List<AlbumStatus> albumStatuses, List<ArtistRole> artistRoles, List<AlbumType> albumTypes, Sort.Direction direction);

    default Flux<AlbumByArtist> findMainAlbumsAll(UUID artistId, List<AlbumStatus> albumStatuses, Sort.Direction direction) {
        return this.findAll(artistId, albumStatuses, List.of(ArtistRole.MAIN_ARTIST), List.of(AlbumType.values()), direction);
    }

    default Flux<AlbumByArtist> findMainAlbumsByType(UUID artistId, List<AlbumStatus> albumStatuses, AlbumType albumType, Sort.Direction direction) {
        return this.findAll(artistId, albumStatuses, List.of(ArtistRole.MAIN_ARTIST), List.of(albumType), direction);
    }

    default Flux<AlbumByArtist> findFeaturedAlbums(UUID artistId, List<AlbumStatus> albumStatuses, Sort.Direction direction) {
        return this.findAll(artistId, albumStatuses, List.of(ArtistRole.FEATURED_ARTIST), List.of(AlbumType.values()), direction);
    }


}
