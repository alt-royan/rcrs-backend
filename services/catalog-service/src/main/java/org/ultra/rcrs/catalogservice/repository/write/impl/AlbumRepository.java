package org.ultra.rcrs.catalogservice.repository.write.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.Album;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;

@Repository
public class AlbumRepository extends ReactiveAbstractWriteRepository<Album> {

    public AlbumRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, Album.class);
    }
}
