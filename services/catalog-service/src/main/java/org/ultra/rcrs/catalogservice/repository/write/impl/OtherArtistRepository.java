package org.ultra.rcrs.catalogservice.repository.write.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;
import org.ultra.rcrs.catalogservice.model.write.OtherArtist;
import org.ultra.rcrs.catalogservice.repository.write.ReactiveAbstractWriteRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
public class OtherArtistRepository extends ReactiveAbstractWriteRepository<OtherArtist> {


    public OtherArtistRepository(@Autowired R2dbcEntityTemplate template) {
        super(template, OtherArtist.class);
    }
}
