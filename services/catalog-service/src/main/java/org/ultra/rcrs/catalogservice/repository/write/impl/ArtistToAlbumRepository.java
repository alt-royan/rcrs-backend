package org.ultra.rcrs.catalogservice.repository.write.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class ArtistToAlbumRepository extends ReactiveAbstractWriteRepository<ArtistToAlbum> {

    public ArtistToAlbumRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, ArtistToAlbum.class);
    }


    public Mono<Long> deleteByAlbumId(UUID albumId) {
        return template.delete(query(where("album_id").is(albumId)), ArtistToAlbum.class);
    }
}
