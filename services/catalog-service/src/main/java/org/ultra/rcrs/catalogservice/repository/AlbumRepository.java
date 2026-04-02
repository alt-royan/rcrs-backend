package org.ultra.rcrs.catalogservice.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.album.Album;

import java.util.UUID;

@Repository
public interface AlbumRepository extends ReactiveCassandraRepository<Album, UUID>, AlbumPersistRepository<Album> {

}
