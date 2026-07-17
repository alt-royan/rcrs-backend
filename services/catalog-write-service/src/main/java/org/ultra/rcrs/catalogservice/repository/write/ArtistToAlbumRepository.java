package org.ultra.rcrs.catalogservice.repository.write;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbumId;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArtistToAlbumRepository extends JpaRepository<ArtistToAlbum, ArtistToAlbumId> {

    long deleteByAlbumId(UUID albumId);

    List<ArtistToAlbum> findByAlbumId(UUID albumId);
}
