package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.ArtistToAlbumPK;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistToAlbumRepository extends JpaRepository<ArtistToAlbum, ArtistToAlbumPK> {

}
