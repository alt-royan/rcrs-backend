package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.ArtistToAlbum;
import org.ultra.rcrs.metadata.model.ArtistToAlbumPK;

@Repository
public interface ArtistToAlbumRepository extends JpaRepository<ArtistToAlbum, ArtistToAlbumPK> {

}
