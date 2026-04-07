package org.ultra.rcrs.catalogservice.repository;


import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.Album;

import java.util.UUID;

@Repository
public interface AlbumRepository extends ReactiveCrudRepository<Album, UUID> {

}
