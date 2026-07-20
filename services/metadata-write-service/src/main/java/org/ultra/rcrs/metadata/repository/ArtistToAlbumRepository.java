package org.ultra.rcrs.metadata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.metadata.model.ArtistToAlbum;
import org.ultra.rcrs.metadata.model.ArtistToAlbumPK;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface ArtistToAlbumRepository extends JpaRepository<ArtistToAlbum, ArtistToAlbumPK> {

    @Modifying
    @Query("DELETE FROM ArtistToAlbum ata WHERE ata.albumId IN :albumIds")
    void deleteByAlbumIdsIn(Collection<UUID> albumIds);
}
